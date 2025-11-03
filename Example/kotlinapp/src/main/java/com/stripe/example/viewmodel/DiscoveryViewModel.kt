package com.stripe.example.viewmodel

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stripe.example.NavigationListener
import com.stripe.example.fragment.discovery.DiscoveryMethod
import com.stripe.example.fragment.discovery.DiscoveryMethod.BLUETOOTH_SCAN
import com.stripe.example.fragment.discovery.DiscoveryMethod.INTERNET
import com.stripe.example.fragment.discovery.DiscoveryMethod.TAP_TO_PAY
import com.stripe.example.fragment.discovery.DiscoveryMethod.USB
import com.stripe.example.fragment.discovery.ReaderClickListener
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.ktx.discoverReaders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class DiscoveryViewModel(
    val discoveryMethod: DiscoveryMethod,
    private val isSimulated: Boolean
) : ViewModel() {
    val readers: MutableLiveData<List<Reader>> = MutableLiveData(listOf())
    val isConnecting: MutableLiveData<Boolean> = MutableLiveData(false)
    val isUpdating: MutableLiveData<Boolean> = MutableLiveData(false)
    val updateProgress: MutableLiveData<Float> = MutableLiveData(0F)
    val selectedLocation = MutableLiveData<Location?>(null)

    var discoveryTask: Cancelable? = null
    var readerClickListener: ReaderClickListener? = null
    var navigationListener: NavigationListener? = null

    private var isRequestingChangeLocation: Boolean = false

    fun requestChangeLocation() {
        isRequestingChangeLocation = true
        stopDiscovery {
            navigationListener?.onRequestLocationSelection()
        }
    }

    private val discoveryConfig: DiscoveryConfiguration
        get() = when (discoveryMethod) {
            BLUETOOTH_SCAN -> DiscoveryConfiguration.BluetoothDiscoveryConfiguration(0, isSimulated)
            INTERNET -> DiscoveryConfiguration.InternetDiscoveryConfiguration(
                location = selectedLocation.value?.id,
                isSimulated = isSimulated,
            )
            TAP_TO_PAY -> DiscoveryConfiguration.TapToPayDiscoveryConfiguration(isSimulated)
            USB -> DiscoveryConfiguration.UsbDiscoveryConfiguration(0, isSimulated)
        }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ],
    )
    fun startDiscovery(onFailure: () -> Unit) {
        viewModelScope.launch {
            Terminal.getInstance().discoverReaders(config = discoveryConfig)
                .catch { e ->
                    if (e is CancellationException) {
                        // Ignore cancellations
                        return@catch
                    }
                    onFailure()
                }
                .collect { discoveredReaders: List<Reader> ->
                    readers.postValue(
                        discoveredReaders.filter { it.networkStatus != Reader.NetworkStatus.OFFLINE }
                    )
                }
        }.also(discoveryJobs::add)
    }

    private val discoveryJobs = mutableListOf<Job>()
    fun stopDiscovery(onSuccess: () -> Unit = { }) {
        viewModelScope.launch {
            discoveryJobs.forEach { it.cancel("Stopping discovery") }
            discoveryJobs.joinAll()
            onSuccess()
        }
    }

    override fun onCleared() {
        super.onCleared()
        readerClickListener = null
        navigationListener = null
        stopDiscovery()
    }
}

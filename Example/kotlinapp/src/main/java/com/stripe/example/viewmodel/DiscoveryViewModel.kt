package com.stripe.example.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stripe.example.NavigationListener
import com.stripe.example.fragment.discovery.ReaderClickListener
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.DiscoveryListener
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import com.stripe.stripeterminal.external.models.DiscoveryMethod
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException

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
            navigationListener?.onRequestChangeLocation()
        }
    }

    fun startDiscovery(onFailure: () -> Unit) {
        if (discoveryTask == null && Terminal.getInstance().connectedReader == null) {
            discoveryTask = Terminal
                .getInstance()
                .discoverReaders(
                    config = DiscoveryConfiguration(
                        0,
                        discoveryMethod,
                        isSimulated,
                        selectedLocation.value?.id
                    ),
                    discoveryListener = object : DiscoveryListener {
                        override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
                            this@DiscoveryViewModel.readers.postValue(
                                readers.filter { it.networkStatus != Reader.NetworkStatus.OFFLINE }
                            )
                        }
                    },
                    callback = object : Callback {
                        override fun onSuccess() {
                            discoveryTask = null
                            isRequestingChangeLocation = false
                        }

                        override fun onFailure(e: TerminalException) {
                            discoveryTask = null
                            if (!isRequestingChangeLocation) {
                                onFailure()
                            }
                            isRequestingChangeLocation = false
                        }
                    }
                )
        }
    }

    fun stopDiscovery(onSuccess: () -> Unit = { }) {
        discoveryTask?.cancel(object : Callback {
            override fun onSuccess() {
                discoveryTask = null
                onSuccess()
            }

            override fun onFailure(e: TerminalException) {
                discoveryTask = null
            }
        }) ?: run {
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

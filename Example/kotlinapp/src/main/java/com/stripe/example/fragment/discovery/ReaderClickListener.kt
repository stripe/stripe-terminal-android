package com.stripe.example.fragment.discovery

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.viewModelScope
import com.stripe.example.MainActivity
import com.stripe.example.R
import com.stripe.example.viewmodel.DiscoveryViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.models.ConnectionConfiguration
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.ktx.connectReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

class ReaderClickListener(
    private val activity: MainActivity,
    private val viewModel: DiscoveryViewModel
) {
    fun onClick(reader: Reader) {
        val connectLocationId = viewModel.selectedLocation.value?.id ?: reader.location?.id

        if (connectLocationId == null) {
            AlertDialog.Builder(activity)
                .setPositiveButton(R.string.alert_acknowledge_button) { _, _ -> }
                .setTitle(R.string.location_required_dialog_title)
                .setMessage(R.string.location_required_dialog_message)
                .show()
            return
        }
        val activityRef = WeakReference(activity)

        viewModel.run {
            val config = when (discoveryMethod) {
                DiscoveryMethod.BLUETOOTH_SCAN ->
                    ConnectionConfiguration.BluetoothConnectionConfiguration(
                        locationId = connectLocationId,
                        bluetoothReaderListener = activityRef.get()!!,
                    )

                DiscoveryMethod.INTERNET ->
                    ConnectionConfiguration.InternetConnectionConfiguration(internetReaderListener = activityRef.get())

                DiscoveryMethod.TAP_TO_PAY ->
                    ConnectionConfiguration.TapToPayConnectionConfiguration(
                        locationId = connectLocationId,
                        autoReconnectOnUnexpectedDisconnect = true,
                        tapToPayReaderListener = activityRef.get()
                    )

                DiscoveryMethod.USB -> ConnectionConfiguration.UsbConnectionConfiguration(
                    locationId = connectLocationId,
                    usbReaderListener = activityRef.get()!!,
                )
            }

            viewModelScope.launch {
                isConnecting.postValue(true)
                val result = runCatching { Terminal.getInstance().connectReader(reader, config) }
                    // rethrow CancellationException to properly cancel the coroutine
                    .onFailure { if (it is CancellationException) throw it }
                withContext(Dispatchers.Main) {
                    // switch to the main thread to update the UI
                    if (result.isSuccess) {
                        activityRef.get()?.onConnectReader()
                        isUpdating.value = false
                        isConnecting.value = false
                    } else {
                        // handle failure
                        val exception = result.exceptionOrNull() as TerminalException
                        Toast.makeText(
                            activityRef.get(),
                            "Failed to connect with error: ${exception.errorCode} ${exception.errorMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                        activityRef.get()?.onCancelDiscovery()
                        isConnecting.value = false
                        isUpdating.value = false
                    }
                }
            }
        }
    }
}

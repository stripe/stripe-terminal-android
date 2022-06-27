package com.stripe.example.fragment.discovery

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.stripe.example.MainActivity
import com.stripe.example.R
import com.stripe.example.viewmodel.DiscoveryViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.UsbConnectivity
import com.stripe.stripeterminal.external.callable.ReaderCallback
import com.stripe.stripeterminal.external.models.ConnectionConfiguration
import com.stripe.stripeterminal.external.models.ConnectionConfiguration.BluetoothConnectionConfiguration
import com.stripe.stripeterminal.external.models.DiscoveryMethod
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException
import java.lang.ref.WeakReference

class ReaderClickListener(
    private val activity: MainActivity,
    private val viewModel: DiscoveryViewModel
) {
    @OptIn(UsbConnectivity::class)
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

        viewModel.isConnecting.value = true

        val activityRef = WeakReference(activity)
        val readerCallback = object : ReaderCallback {
            override fun onSuccess(reader: Reader) {
                activityRef.get()?.let {
                    it.runOnUiThread {
                        it.onConnectReader()
                        viewModel.isConnecting.value = false
                        viewModel.isUpdating.value = false
                    }
                }
            }

            override fun onFailure(e: TerminalException) {
                activityRef.get()?.let {
                    it.runOnUiThread {
                        Toast.makeText(
                            it,
                            "Failed to connect with error: " + e.errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                        it.onCancelDiscovery()
                        viewModel.isConnecting.value = false
                        viewModel.isUpdating.value = false
                    }
                }
            }
        }
        when (viewModel.discoveryMethod) {
            DiscoveryMethod.BLUETOOTH_SCAN -> {
                Terminal.getInstance().connectBluetoothReader(
                    reader,
                    BluetoothConnectionConfiguration(connectLocationId),
                    activityRef.get(),
                    readerCallback,
                )
            }
            DiscoveryMethod.INTERNET -> {
                Terminal.getInstance().connectInternetReader(
                    reader,
                    ConnectionConfiguration.InternetConnectionConfiguration(),
                    readerCallback,
                )
            }
            DiscoveryMethod.USB -> {
                Terminal.getInstance().connectUsbReader(
                    reader,
                    ConnectionConfiguration.UsbConnectionConfiguration(connectLocationId),
                    activityRef.get(),
                    readerCallback,
                )
            }
            else -> Log.w(javaClass.simpleName, "Trying to connect to unsupported reader")
        }
    }
}

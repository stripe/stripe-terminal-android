package com.stripe.example.fragment.discovery

import androidx.appcompat.app.AlertDialog
import com.stripe.example.MainActivity
import com.stripe.example.R
import com.stripe.example.viewmodel.DiscoveryViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.ReaderCallback
import com.stripe.stripeterminal.external.models.ConnectionConfiguration.BluetoothConnectionConfiguration
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException
import java.lang.ref.WeakReference

class ReaderClickListener(
    var activityRef: WeakReference<MainActivity>,
    private val viewModel: DiscoveryViewModel
) {
    fun onClick(reader: Reader) {
        val activity = activityRef.get() ?: return
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
        Terminal.getInstance().connectBluetoothReader(
            reader,
            BluetoothConnectionConfiguration(connectLocationId),
            activityRef.get(),
            object : ReaderCallback {
                override fun onSuccess(reader: Reader) {
                    activityRef.get()?.let {
                        it.runOnUiThread {
                            viewModel.isConnecting.value = false
                            viewModel.isUpdating.value = false
                            it.onConnectReader()
                        }
                    }
                }

                override fun onFailure(e: TerminalException) {
                    activityRef.get()?.let {
                        it.runOnUiThread {
                            viewModel.isConnecting.value = false
                            viewModel.isUpdating.value = false
                            it.onCancelDiscovery()
                        }
                    }
                }
            }
        )
    }
}

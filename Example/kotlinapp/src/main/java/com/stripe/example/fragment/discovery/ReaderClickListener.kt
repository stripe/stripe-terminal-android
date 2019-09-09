package com.stripe.example.fragment.discovery

import com.stripe.example.MainActivity
import com.stripe.example.viewmodel.DiscoveryViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.callable.ReaderCallback
import com.stripe.stripeterminal.model.external.Reader
import com.stripe.stripeterminal.model.external.TerminalException
import java.lang.ref.WeakReference

class ReaderClickListener(
    var activityRef: WeakReference<MainActivity>,
    private val viewModel: DiscoveryViewModel
) {
    fun onClick(reader: Reader) {
        viewModel.isConnecting.value = true
        Terminal.getInstance().connectReader(reader, object : ReaderCallback {
            override fun onSuccess(reader: Reader) {
                activityRef.get()?.let {
                    it.runOnUiThread {
                        viewModel.isConnecting.value = false
                        it.onConnectReader()
                    }
                }
            }

            override fun onFailure(e: TerminalException) {
                activityRef.get()?.let {
                    it.runOnUiThread {
                        viewModel.isConnecting.value = false
                        it.onCancelDiscovery()
                    }
                }
            }
        })
    }
}

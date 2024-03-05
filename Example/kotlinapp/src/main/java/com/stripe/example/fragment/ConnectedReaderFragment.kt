package com.stripe.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.TerminalOfflineListener
import com.stripe.example.customviews.TerminalOnlineIndicator
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.OfflineMode
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.models.NetworkStatus
import com.stripe.stripeterminal.external.models.TerminalException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * The `ConnectedReaderFragment` displays the reader that's currently connected and provides
 * options for workflows that can be executed.
 */
@OptIn(OfflineMode::class)
class ConnectedReaderFragment : Fragment() {

    companion object {
        const val TAG = "com.stripe.example.fragment.ConnectedReaderFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_connected_reader, container, false)

        // Set the description of the connected reader
        Terminal.getInstance().connectedReader?.let {
            view.findViewById<TextView>(R.id.reader_description).text = getString(
                R.string.reader_description,
                it.deviceType,
                it.serialNumber,
            )
            // TODO: Set status as well
        }

        // Set up the disconnect button
        val activityRef = WeakReference(activity)
        view.findViewById<View>(R.id.disconnect_button).setOnClickListener {
            Terminal.getInstance().disconnectReader(object : Callback {

                override fun onSuccess() {
                    activityRef.get()?.let {
                        if (it is NavigationListener) {
                            it.runOnUiThread {
                                it.onDisconnectReader()
                            }
                        }
                    }
                }

                override fun onFailure(e: TerminalException) {
                }
            })
        }

        launchAndRepeatWithViewLifecycle(Lifecycle.State.RESUMED) {
            launch {
                TerminalOfflineListener.offlineStatus
                        .collectLatest {
                            updateTerminalOnlineIndicator(it)
                        }
            }
        }

        updateTerminalOnlineIndicator(Terminal.getInstance().offlineStatus.sdk.networkStatus)

        // Set up the collect payment button
        view.findViewById<View>(R.id.collect_card_payment_button).setOnClickListener {
            (activity as? NavigationListener)?.onSelectPaymentWorkflow()
        }

        // Set up the save card button
        view.findViewById<View>(R.id.save_card_button).setOnClickListener {
            (activity as? NavigationListener)?.onRequestSaveCard()
        }

        // Set up the update reader button
        view.findViewById<View>(R.id.update_reader_button).setOnClickListener {
            (activity as? NavigationListener)?.onSelectUpdateWorkflow()
        }

        // Set up the view offline logs button
        view.findViewById<View>(R.id.view_offline_logs_button).setOnClickListener {
            (activity as? NavigationListener)?.onSelectViewOfflineLogs()
        }

        return view
    }

    private fun updateTerminalOnlineIndicator(networkStatus: NetworkStatus) {
        view?.findViewById<TerminalOnlineIndicator>(R.id.online_indicator).run {
            this?.networkStatus = networkStatus
        }
    }
}

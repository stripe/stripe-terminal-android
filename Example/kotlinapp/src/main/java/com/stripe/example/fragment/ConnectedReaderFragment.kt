package com.stripe.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.models.TerminalException
import java.lang.ref.WeakReference

/**
 * The `ConnectedReaderFragment` displays the reader that's currently connected and provides
 * options for workflows that can be executed.
 */
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
                it.deviceType, it.serialNumber
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

        // Set up the collect payment button
        view.findViewById<View>(R.id.collect_card_payment_button).setOnClickListener {
            (activity as? NavigationListener)?.onSelectPaymentWorkflow()
        }

        // Set up the read reusable card button
        view.findViewById<View>(R.id.read_reusable_card_button).setOnClickListener {
            (activity as? NavigationListener)?.onSelectReadReusableCardWorkflow()
        }

        // Set up the update reader button
        view.findViewById<View>(R.id.update_reader_button).setOnClickListener {
            (activity as? NavigationListener)?.onSelectUpdateWorkflow()
        }

        return view
    }
}

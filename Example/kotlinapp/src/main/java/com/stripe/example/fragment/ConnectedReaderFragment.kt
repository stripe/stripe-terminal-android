package com.stripe.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.callable.Callback
import com.stripe.stripeterminal.model.external.TerminalException
import java.lang.ref.WeakReference
import kotlinx.android.synthetic.main.fragment_connected_reader.view.collect_card_payment_button
import kotlinx.android.synthetic.main.fragment_connected_reader.view.disconnect_button
import kotlinx.android.synthetic.main.fragment_connected_reader.view.read_reusable_card_button
import kotlinx.android.synthetic.main.fragment_connected_reader.view.reader_description
import kotlinx.android.synthetic.main.fragment_connected_reader.view.update_reader_button

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
            view.reader_description.text = getString(R.string.reader_description,
                    it.deviceType, it.serialNumber)
            // TODO: Set status as well
        }

        // Set up the disconnect button
        val activityRef = WeakReference(activity)
        view.disconnect_button.setOnClickListener {
            Terminal.getInstance().disconnectReader(object : Callback {

                override fun onSuccess() {
                    activityRef.get()?.let {
                        if (it is NavigationListener) {
                            it.onDisconnectReader()
                        }
                    }
                }

                override fun onFailure(e: TerminalException) {
                }
            })
        }

        // Set up the collect payment button
        view.collect_card_payment_button.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onSelectPaymentWorkflow()
            }
        }

        // Set up the read reusable card button
        view.read_reusable_card_button.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onSelectReadReusableCardWorkflow()
            }
        }

        // Set up the update reader button
        view.update_reader_button.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onSelectUpdateWorkflow()
            }
        }

        return view
    }
}

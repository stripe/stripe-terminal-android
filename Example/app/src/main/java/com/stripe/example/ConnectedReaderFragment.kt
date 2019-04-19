package com.stripe.example


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.stripe.stripeterminal.Terminal
import kotlinx.android.synthetic.main.fragment_connected_reader.view.*

/**
 * The `ConnectedReaderFragment` displays the reader that's currently connected and provides
 * options for workflows that can be executed.
 */
class ConnectedReaderFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_connected_reader, container, false)

        // Set the description of the connected reader
        Terminal.getInstance().connectedReader?.let {
            view.reader_description.text = getString(R.string.reader_description,
                    it.deviceType.name, it.serialNumber)
            // TODO: Set status as well
        }

        // Set up the disconnect button
        view.disconnect_button.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onRequestDisconnect()
            }
        }

        // Set up the collect payment button
        view.collect_card_payment_button.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onSelectPaymentWorkflow()
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

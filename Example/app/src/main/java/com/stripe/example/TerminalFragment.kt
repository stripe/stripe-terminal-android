package com.stripe.example

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_terminal.view.*

/**
 * The `TerminalFragment` is the main [Fragment] shown in the app, and handles navigation to any
 * other [Fragment]s as necessary.
 */
class TerminalFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_terminal, container, false)

        // Link up the discovery button
        view.discover_button.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onRequestDiscovery()
            }
        }

        // TODO: Do this dynamically from the type selected
        view.device_type_button.setText(R.string.chipper_2x)

        return view
    }
}


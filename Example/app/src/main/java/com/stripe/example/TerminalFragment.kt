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

    companion object {
        private const val SIMULATED_SWITCH = "simulated_switch"
    }

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

        // Link up the simulated switch
        view.simulated_switch.isChecked = arguments?.getBoolean(SIMULATED_SWITCH) ?: false
        view.simulated_switch.setOnCheckedChangeListener { _, isOn ->
            if (activity is NavigationListener) {
                (activity as NavigationListener).onToggleSimulatedSwitch(isOn)
            }
        }

        // TODO: Do this dynamically from the type selected
        view.device_type_button.setText(R.string.chipper_2x)

        return view
    }

    fun setSimulatedSwitch(isOn: Boolean) : TerminalFragment {
        val args = Bundle()
        args.putBoolean(SIMULATED_SWITCH, isOn)
        this.arguments = args
        return this
    }
}


package com.stripe.example.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.edit
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.FragmentTerminalBinding
import com.stripe.example.fragment.discovery.DiscoveryMethod
import com.stripe.example.viewmodel.TerminalViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The `TerminalFragment` is the main [Fragment] shown in the app, and handles navigation to any
 * other [Fragment]s as necessary.
 */
class TerminalFragment : Fragment(R.layout.fragment_terminal) {

    companion object {
        const val TAG = "com.stripe.example.fragment.TerminalFragment"

        // A string to store if the simulated switch is set
        private const val SIMULATED_SWITCH_KEY = "simulated_switch"

        // A string to store the selected discovery method
        private const val DISCOVERY_METHOD_KEY = "discovery_method"
    }

    private lateinit var viewModel: TerminalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel = TerminalViewModel(
                BundleCompat.getSerializable(
                    it,
                    DISCOVERY_METHOD_KEY,
                    DiscoveryMethod::class.java
                ) ?: DiscoveryMethod.entries.first(),
                it.getBoolean(SIMULATED_SWITCH_KEY),
            )
        } ?: run {
            CoroutineScope(Dispatchers.IO).launch {
                val isSimulated = activity?.getSharedPreferences(
                    TAG,
                    Context.MODE_PRIVATE
                )?.getBoolean(SIMULATED_SWITCH_KEY, false) ?: false
                val discoveryMethod = activity?.getSharedPreferences(
                    TAG,
                    Context.MODE_PRIVATE
                )?.getInt(DISCOVERY_METHOD_KEY, 0) ?: 0
                viewModel = TerminalViewModel(
                    DiscoveryMethod.entries[discoveryMethod],
                    isSimulated,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTerminalBinding.bind(view)

        // Set the device type spinner
        binding.discoveryMethodSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            DiscoveryMethod.entries,
        )

        // Set initial values from viewModel
        binding.discoveryMethodSpinner.setSelection(viewModel.discoveryMethodPosition)
        binding.simulatedSwitch.isChecked = viewModel.simulated

        // Set up spinner listener for two-way binding
        binding.discoveryMethodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.discoveryMethodPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        // Set up switch listener for two-way binding
        binding.simulatedSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.simulated = isChecked
        }

        // Link up the discovery button
        binding.discoverButton.setOnClickListener {
            (activity as? NavigationListener)?.onRequestDiscovery(
                viewModel.simulated,
                viewModel.discoveryMethod,
            )
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.let {
            it.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit {
                putBoolean(SIMULATED_SWITCH_KEY, viewModel.simulated)
                putInt(DISCOVERY_METHOD_KEY, viewModel.discoveryMethodPosition)
            }
        }
    }
}

package com.stripe.example.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.FragmentTerminalBinding
import com.stripe.example.viewmodel.TerminalViewModel
import com.stripe.stripeterminal.external.models.DiscoveryMethod
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
        private const val SIMULATED_SWITCH = "simulated_switch"

        // A string to store the selected discovery method
        private const val DISCOVERY_METHOD = "discovery_method"
        private val discoveryMethods =
            listOf(DiscoveryMethod.BLUETOOTH_SCAN, DiscoveryMethod.INTERNET, DiscoveryMethod.USB)

        fun getCurrentDiscoveryMethod(activity: Activity?): DiscoveryMethod {
            val pos = activity?.getSharedPreferences(TAG, Context.MODE_PRIVATE)
                ?.getInt(DISCOVERY_METHOD, 0) ?: 0

            return discoveryMethods[pos]
        }
    }

    private lateinit var viewModel: TerminalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel = TerminalViewModel(
                it.getSerializable(DISCOVERY_METHOD) as DiscoveryMethod,
                discoveryMethods,
                it.getBoolean(SIMULATED_SWITCH)
            )
        } ?: run {
            CoroutineScope(Dispatchers.IO).launch {
                val isSimulated = activity?.getSharedPreferences(
                    TAG,
                    Context.MODE_PRIVATE
                )?.getBoolean(SIMULATED_SWITCH, false) ?: false
                val discoveryMethod = activity?.getSharedPreferences(
                    TAG,
                    Context.MODE_PRIVATE
                )?.getInt(DISCOVERY_METHOD, 0) ?: 0
                viewModel =
                    TerminalViewModel(discoveryMethods[discoveryMethod], discoveryMethods, isSimulated)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inflate the layout for this fragment
        val viewBinding = requireNotNull(
            DataBindingUtil.bind<FragmentTerminalBinding>(view)
        )
        viewBinding.lifecycleOwner = viewLifecycleOwner
        viewBinding.viewModel = viewModel

        // Set the device type spinner
        viewBinding.discoveryMethodSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            discoveryMethods
        )

        // Link up the discovery button
        viewBinding.discoverButton.setOnClickListener {
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
                putBoolean(SIMULATED_SWITCH, viewModel.simulated)
                putInt(DISCOVERY_METHOD, viewModel.discoveryMethodPosition)
            }
        }
    }
}

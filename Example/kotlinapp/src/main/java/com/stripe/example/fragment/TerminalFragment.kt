package com.stripe.example.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.FragmentTerminalBinding
import com.stripe.example.viewmodel.TerminalViewModel
import kotlinx.android.synthetic.main.fragment_terminal.*
import kotlinx.android.synthetic.main.fragment_terminal.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The `TerminalFragment` is the main [Fragment] shown in the app, and handles navigation to any
 * other [Fragment]s as necessary.
 */
class TerminalFragment : Fragment() {

    companion object {
        const val TAG = "com.stripe.example.fragment.TerminalFragment"

        // A string to store if the simulated switch is set
        private const val SIMULATED_SWITCH = "simulated_switch"
    }

    private lateinit var binding: FragmentTerminalBinding
    private lateinit var viewModel: TerminalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel = TerminalViewModel(it.getBoolean(SIMULATED_SWITCH))
        } ?: run {
            CoroutineScope(Dispatchers.IO).launch {
                val isSimulated = activity?.getSharedPreferences(TAG,
                        Context.MODE_PRIVATE)?.getBoolean(SIMULATED_SWITCH, false) ?: false
                viewModel = TerminalViewModel(isSimulated)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_terminal, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Link up the discovery button
        discover_button.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onRequestDiscovery(viewModel.simulated)
            }
        }

        // TODO: Do this dynamically from the type selected
        view.device_type_button.setText(R.string.chipper_2x)
    }

    override fun onPause() {
        super.onPause()
        activity?.getSharedPreferences(TAG, Context.MODE_PRIVATE)?.edit()
                ?.putBoolean(SIMULATED_SWITCH, viewModel.simulated)
                ?.apply()
    }
}

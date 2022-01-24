package com.stripe.example.fragment.discovery

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.stripe.example.MainActivity
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.FragmentDiscoveryBinding
import com.stripe.example.fragment.location.LocationSelectionController
import com.stripe.example.viewmodel.DiscoveryViewModel
import com.stripe.stripeterminal.external.callable.BluetoothReaderListener
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.models.DiscoveryMethod
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate
import com.stripe.stripeterminal.external.models.TerminalException
import java.lang.ref.WeakReference

/**
 * The `DiscoveryFragment` shows the list of recognized readers and allows the user to
 * select one to connect to.
 */
class DiscoveryFragment :
    Fragment(R.layout.fragment_discovery),
    BluetoothReaderListener,
    LocationSelectionController {

    companion object {
        private const val DISCOVERY_METHOD = "discovery_method"
        private const val SIMULATED_KEY = "simulated"

        const val TAG = "com.stripe.example.fragment.discovery.DiscoveryFragment"

        fun newInstance(simulated: Boolean, discoveryMethod: DiscoveryMethod): DiscoveryFragment {
            val fragment = DiscoveryFragment()
            val bundle = Bundle()
            bundle.putBoolean(SIMULATED_KEY, simulated)
            bundle.putSerializable(DISCOVERY_METHOD, discoveryMethod)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var viewModel: DiscoveryViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val discoveryViewModelFactory = DiscoveryViewModelFactory(requireArguments())
        viewModel =
            ViewModelProvider(this, discoveryViewModelFactory)[DiscoveryViewModel::class.java]

        viewModel.readerClickListener = ReaderClickListener(
            activity as MainActivity,
            viewModel
        )

        viewModel.navigationListener = activity as NavigationListener

        val viewBinding = requireNotNull(
            DataBindingUtil.bind<FragmentDiscoveryBinding>(view)
        )
        viewBinding.lifecycleOwner = viewLifecycleOwner
        viewBinding.viewModel = viewModel

        val readerRecyclerView = viewBinding.readerRecyclerView
        readerRecyclerView.layoutManager = LinearLayoutManager(activity)

        val adapter = ReaderAdapter(viewModel, layoutInflater)
        readerRecyclerView.adapter = adapter

        viewBinding.cancelButton.setOnClickListener {
            val activityRef = WeakReference(activity as MainActivity)
            viewModel.stopDiscovery { activityRef.get()?.onCancelDiscovery() }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val activityRef = WeakReference(activity as MainActivity)
            viewModel.startDiscovery { activityRef.get()?.onCancelDiscovery() }
        }

        viewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            adapter.updateLocationSelection(location)

            if (location != null) {
                val activityRef = WeakReference(activity as MainActivity)
                viewModel.startDiscovery { activityRef.get()?.onCancelDiscovery() }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.readerClickListener = null
        viewModel.navigationListener = null
        viewModel.stopDiscovery()
    }

    override fun onLocationSelected(location: Location) {
        viewModel.selectedLocation.value = location
    }

    override fun onLocationCleared() {
        viewModel.selectedLocation.value = null
    }

    override fun onStartInstallingUpdate(update: ReaderSoftwareUpdate, cancelable: Cancelable?) {
        Log.d("DiscoveryFragment", "onStartInstallingUpdate")
        viewModel.isConnecting.value = false
        viewModel.isUpdating.value = true
        viewModel.discoveryTask = cancelable
    }

    override fun onReportReaderSoftwareUpdateProgress(progress: Float) {
        // Convert to percentage
        Log.d("DiscoveryFragment", "onReportReaderSoftwareUpdateProgress: $progress")
        viewModel.updateProgress.value = progress * 100
    }

    override fun onFinishInstallingUpdate(update: ReaderSoftwareUpdate?, e: TerminalException?) {
        Log.d("DiscoveryFragment", "onFinishInstallingUpdate")
    }

    class DiscoveryViewModelFactory(private val args: Bundle) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiscoveryViewModel(
                args.getSerializable(DISCOVERY_METHOD) as DiscoveryMethod,
                args.getBoolean(SIMULATED_KEY)
            ) as T
        }
    }
}

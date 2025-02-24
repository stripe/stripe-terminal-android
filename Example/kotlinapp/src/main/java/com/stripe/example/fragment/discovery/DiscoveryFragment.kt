package com.stripe.example.fragment.discovery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.stripe.example.MainActivity
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.FragmentDiscoveryBinding
import com.stripe.example.fragment.launchAndRepeatWithViewLifecycle
import com.stripe.example.fragment.location.LocationSelectionController
import com.stripe.example.viewmodel.DiscoveryViewModel
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.MobileReaderListener
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate
import com.stripe.stripeterminal.external.models.TerminalException

/**
 * The `DiscoveryFragment` shows the list of recognized readers and allows the user to
 * select one to connect to.
 */
class DiscoveryFragment :
    Fragment(R.layout.fragment_discovery),
    MobileReaderListener,
    LocationSelectionController {

    // Register the permissions callback to handles the response to the system permissions dialog.
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        ::onPermissionResult,
    )

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
            viewModel.stopDiscovery { (requireActivity() as MainActivity).onCancelDiscovery() }
        }

        launchAndRepeatWithViewLifecycle {
            startDiscovery()
        }

        viewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            adapter.updateLocationSelection(location)

            if (location != null) {
                startDiscovery()
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

    private fun onPermissionResult(permissions: Map<String, Boolean>) {
        // If none of the requested permissions were declined, start the discovery process.
        if (permissions.none { !it.value }) {
            startDiscovery()
        } else {
            (requireActivity() as MainActivity).onCancelDiscovery()
        }
    }

    private fun startDiscovery() {
        if (checkPermission(viewModel.discoveryMethod)) {
            viewModel.startDiscovery { (requireActivity() as MainActivity).onCancelDiscovery() }
        }
    }

    private fun checkPermission(discoveryMethod: DiscoveryMethod): Boolean {
        val hasGpsModule = requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        val locationPermission = if (hasGpsModule) {
            Manifest.permission.ACCESS_FINE_LOCATION
        } else {
            Manifest.permission.ACCESS_COARSE_LOCATION
        }

        val ungrantedPermissions = buildList {
            if (!isGranted(locationPermission)) add(locationPermission)

            if (discoveryMethod == DiscoveryMethod.BLUETOOTH_SCAN && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!isGranted(Manifest.permission.BLUETOOTH_SCAN)) add(Manifest.permission.BLUETOOTH_SCAN)
                if (!isGranted(Manifest.permission.BLUETOOTH_CONNECT)) add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }.toTypedArray()

        return if (ungrantedPermissions.isNotEmpty()) {
            // If we don't have all the required permissions yet, request them before doing anything else.
            requestPermissionLauncher.launch(ungrantedPermissions)
            false
        } else {
            true
        }
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
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

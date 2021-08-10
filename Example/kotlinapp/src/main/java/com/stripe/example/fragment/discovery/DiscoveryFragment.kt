package com.stripe.example.fragment.discovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.MainActivity
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.FragmentDiscoveryBinding
import com.stripe.example.fragment.location.LocationSelectionController
import com.stripe.example.viewmodel.DiscoveryViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.BluetoothReaderListener
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.DiscoveryListener
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import com.stripe.stripeterminal.external.models.DiscoveryMethod.BLUETOOTH_SCAN
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate
import com.stripe.stripeterminal.external.models.TerminalException
import java.lang.ref.WeakReference

/**
 * The `DiscoveryFragment` shows the list of recognized readers and allows the user to
 * select one to connect to.
 */
class DiscoveryFragment : Fragment(), DiscoveryListener, BluetoothReaderListener, LocationSelectionController {

    companion object {
        private const val SIMULATED_KEY = "simulated"

        const val TAG = "com.stripe.example.fragment.discovery.DiscoveryFragment"

        fun newInstance(simulated: Boolean): DiscoveryFragment {
            val fragment = DiscoveryFragment()
            val bundle = Bundle()
            bundle.putBoolean(SIMULATED_KEY, simulated)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var binding: FragmentDiscoveryBinding
    private lateinit var adapter: ReaderAdapter
    private lateinit var readerRecyclerView: RecyclerView
    private lateinit var viewModel: DiscoveryViewModel
    private lateinit var activityRef: WeakReference<MainActivity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[DiscoveryViewModel::class.java]
        activityRef = WeakReference(activity as MainActivity)
        viewModel.navigationListener = activity as NavigationListener
        viewModel.readerClickListener?.let {
            it.activityRef = activityRef
        } ?: run {
            viewModel.readerClickListener = ReaderClickListener(activityRef, viewModel)
        }

        val discoveryCallback = object : Callback {
            override fun onSuccess() {
                viewModel.discoveryTask = null
            }

            override fun onFailure(e: TerminalException) {
                viewModel.discoveryTask = null
                activityRef.get()?.onCancelDiscovery()
            }
        }

        arguments?.let {
            val config = DiscoveryConfiguration(0, BLUETOOTH_SCAN, it.getBoolean(SIMULATED_KEY))
            if (viewModel.discoveryTask == null && Terminal.getInstance().connectedReader == null) {
                viewModel.discoveryTask = Terminal
                    .getInstance()
                    .discoverReaders(config, this, discoveryCallback)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_discovery, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        readerRecyclerView = binding.readerRecyclerView
        readerRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.viewModel = viewModel

        adapter = ReaderAdapter(viewModel, inflater)
        readerRecyclerView.adapter = adapter

        binding.cancelButton.setOnClickListener {
            viewModel.discoveryTask?.cancel(object : Callback {
                override fun onSuccess() {
                    viewModel.discoveryTask = null
                    activityRef.get()?.onCancelDiscovery()
                }

                override fun onFailure(e: TerminalException) {
                    viewModel.discoveryTask = null
                }
            })
        }

        return binding.root
    }

    override fun onStartInstallingUpdate(update: ReaderSoftwareUpdate, cancelable: Cancelable?) {
        viewModel.isConnecting.value = false
        viewModel.isUpdating.value = true
        viewModel.discoveryTask = cancelable
    }

    override fun onReportReaderSoftwareUpdateProgress(progress: Float) {
        viewModel.updateProgress.value = progress
    }

    override fun onFinishInstallingUpdate(update: ReaderSoftwareUpdate?, e: TerminalException?) { }

    override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
        activityRef.get()?.runOnUiThread {
            viewModel.readers.value = readers
        }
    }

    override fun onLocationSelected(location: Location) {
        viewModel.selectedLocation.value = location
        adapter.updateLocationSelection(location)
    }

    override fun onLocationCleared() {
        viewModel.selectedLocation.value = null
        adapter.updateLocationSelection(null)
    }
}

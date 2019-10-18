package com.stripe.example.fragment.discovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.MainActivity
import com.stripe.example.R
import com.stripe.example.databinding.FragmentDiscoveryBinding
import com.stripe.example.viewmodel.DiscoveryViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.callable.Callback
import com.stripe.stripeterminal.callable.DiscoveryListener
import com.stripe.stripeterminal.model.external.DeviceType
import com.stripe.stripeterminal.model.external.DiscoveryConfiguration
import com.stripe.stripeterminal.model.external.Reader
import com.stripe.stripeterminal.model.external.TerminalException
import java.lang.ref.WeakReference
import kotlinx.android.synthetic.main.fragment_discovery.view.cancel_button
import kotlinx.android.synthetic.main.fragment_discovery.view.reader_recycler_view

/**
 * The `DiscoveryFragment` shows the list of recognized readers and allows the user to
 * select one to connect to.
 */
class DiscoveryFragment : Fragment(), DiscoveryListener {

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
        viewModel = ViewModelProviders.of(this).get(DiscoveryViewModel::class.java)
        activityRef = WeakReference(activity as MainActivity)
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
            val config = DiscoveryConfiguration(0, DeviceType.CHIPPER_2X, it.getBoolean(SIMULATED_KEY))
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
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_discovery, container, false)
        binding.lifecycleOwner = this
        readerRecyclerView = binding.root.reader_recycler_view
        readerRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.viewModel = viewModel

        adapter = ReaderAdapter(viewModel)
        readerRecyclerView.adapter = adapter

        binding.root.cancel_button.setOnClickListener {
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

    override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
        activityRef.get()?.runOnUiThread {
            viewModel.readers.value = readers
        }
    }
}

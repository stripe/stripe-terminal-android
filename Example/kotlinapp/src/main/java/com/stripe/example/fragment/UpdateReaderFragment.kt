package com.stripe.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.stripe.example.MainActivity
import com.stripe.example.R
import com.stripe.example.databinding.FragmentUpdateReaderBinding
import com.stripe.example.viewmodel.UpdateReaderViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.BluetoothReaderListener
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate
import com.stripe.stripeterminal.external.models.TerminalException
import java.lang.ref.WeakReference

/**
 * The `UpdateReaderFragment` allows the user to check the current version of the [Reader] software,
 * as well as update it when necessary.
 */
class UpdateReaderFragment : Fragment(), BluetoothReaderListener {

    companion object {
        const val TAG = "com.stripe.example.fragment.UpdateReaderFragment"
    }

    private lateinit var binding: FragmentUpdateReaderBinding
    private lateinit var viewModel: UpdateReaderViewModel
    private lateinit var activityRef: WeakReference<MainActivity>

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        viewModel = ViewModelProvider(this)[UpdateReaderViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_update_reader, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        if (viewModel.reader == null) {
            viewModel.reader = Terminal.getInstance().connectedReader
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Cancel update on button click
        activityRef = WeakReference(activity as MainActivity)
        binding.cancelButton.setOnClickListener {
            viewModel.installOperation?.cancel(object : Callback {
                override fun onSuccess() {
                    exitWorkflow(activityRef)
                }

                override fun onFailure(e: TerminalException) {
                    exitWorkflow(activityRef)
                }
            }) ?: run {
                exitWorkflow(activityRef)
            }
        }

        // We overload the "check_for_update_button" for multiple uses
        // Fetch update on button click
        binding.checkForUpdateButton.setOnClickListener {
            // If we haven't checked if there is an update, check
            if (!viewModel.hasStartedFetchingUpdate.value!!) {
                viewModel.hasStartedFetchingUpdate.value = true
                activityRef.get()?.runOnUiThread {
                    onUpdateAvailable(Terminal.getInstance().connectedReader?.availableUpdate)
                }
                // If we have an update ready, and we haven't installed it, do so
            } else if (viewModel.hasFinishedFetchingUpdate.value!!) {
                viewModel.readerSoftwareUpdate.value?.let {
                    viewModel.hasStartedInstallingUpdate.value = true
                    Terminal.getInstance().installAvailableUpdate()
                }
            }
        }

        // Done button onClick listeners
        binding.doneButton.setOnClickListener { exitWorkflow(activityRef) }
    }

    fun onCompleteUpdate() {
        viewModel.hasFinishedInstallingUpdate.value = true
    }

    fun onUpdateAvailable(update: ReaderSoftwareUpdate?) {
        viewModel.readerSoftwareUpdate.value = update
        viewModel.hasFinishedFetchingUpdate.value = true
    }

    override fun onReportReaderSoftwareUpdateProgress(progress: Float) {
        viewModel.progress.value = progress
    }

    override fun onStartInstallingUpdate(update: ReaderSoftwareUpdate, cancelable: Cancelable?) {
        viewModel.installOperation = cancelable
    }

    override fun onFinishInstallingUpdate(update: ReaderSoftwareUpdate?, e: TerminalException?) {
        if (e == null) {
            onCompleteUpdate()
        }
        viewModel.installOperation = null
    }

    private fun exitWorkflow(activityRef: WeakReference<MainActivity>) {
        activityRef.get()?.let { activity ->
            activity.runOnUiThread {
                activity.onRequestExitWorkflow()
            }
        }
    }
}

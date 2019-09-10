package com.stripe.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.stripe.example.MainActivity
import com.stripe.example.R
import com.stripe.example.databinding.FragmentUpdateReaderBinding
import com.stripe.example.viewmodel.UpdateReaderViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.callable.Callback
import com.stripe.stripeterminal.callable.ReaderSoftwareUpdateCallback
import com.stripe.stripeterminal.callable.ReaderSoftwareUpdateListener
import com.stripe.stripeterminal.model.external.Reader
import com.stripe.stripeterminal.model.external.ReaderSoftwareUpdate
import com.stripe.stripeterminal.model.external.TerminalException
import java.lang.ref.WeakReference
import kotlinx.android.synthetic.main.fragment_update_reader.cancel_button
import kotlinx.android.synthetic.main.fragment_update_reader.check_for_update_button
import kotlinx.android.synthetic.main.fragment_update_reader.done_button

/**
 * The `UpdateReaderFragment` allows the user to check the current version of the [Reader] software,
 * as well as update it when necessary.
 */
class UpdateReaderFragment : Fragment(), ReaderSoftwareUpdateListener {

    companion object {
        const val TAG = "com.stripe.example.fragment.UpdateReaderFragment"
    }

    private lateinit var binding: FragmentUpdateReaderBinding
    private lateinit var viewModel: UpdateReaderViewModel
    private lateinit var activityRef: WeakReference<MainActivity>

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        viewModel = ViewModelProviders.of(this).get(UpdateReaderViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_update_reader, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        if (viewModel.reader == null) {
            viewModel.reader = Terminal.getInstance().connectedReader
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Cancel update on button click
        activityRef = WeakReference(activity as MainActivity)
        cancel_button.setOnClickListener {
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
        check_for_update_button.setOnClickListener {
            // If we haven't checked if there is an update, check
            if (!viewModel.hasStartedFetchingUpdate.value!!) {
                viewModel.hasStartedFetchingUpdate.value = true
                viewModel.fetchUpdateOperation = Terminal.getInstance()
                        .checkForUpdate(object : ReaderSoftwareUpdateCallback {
                            override fun onSuccess(update: ReaderSoftwareUpdate?) {
                                activityRef.get()?.runOnUiThread {
                                    viewModel.fetchUpdateOperation = null
                                    onUpdateAvailable(update)
                                }
                            }

                            override fun onFailure(e: TerminalException) {
                                activityRef.get()?.runOnUiThread {
                                    viewModel.fetchUpdateOperation = null
                                }
                            }
                        })
                // If we have an update ready, and we haven't installed it, do so
            } else if (viewModel.hasFinishedFetchingUpdate.value!!) {
                viewModel.readerSoftwareUpdate.value?.let {
                    viewModel.hasStartedInstallingUpdate.value = true
                    viewModel.installOperation = Terminal.getInstance().installUpdate(it, this, object : Callback {
                        override fun onSuccess() {
                            activityRef.get()?.runOnUiThread {
                                onCompleteUpdate()
                                viewModel.installOperation = null
                            }
                        }

                        override fun onFailure(e: TerminalException) {
                            activityRef.get()?.runOnUiThread {
                                viewModel.installOperation = null
                            }
                        }
                    })
                }
            }
        }

        // Done button onClick listeners
        done_button.setOnClickListener { exitWorkflow(activityRef) }
    }

    fun onCompleteUpdate() {
        viewModel.hasFinishedInstallingUpdate.value = true
    }

    fun onUpdateAvailable(update: ReaderSoftwareUpdate?) {
        viewModel.readerSoftwareUpdate.value = update
        viewModel.hasFinishedFetchingUpdate.value = true
    }

    override fun onReportReaderSoftwareUpdateProgress(progress: Float) {
        activityRef.get()?.runOnUiThread {
            viewModel.progress.value = progress.toDouble()
        }
    }

    private fun exitWorkflow(activityRef: WeakReference<MainActivity>) {
        activityRef.get()?.let { activity ->
            activity.runOnUiThread {
                activity.onRequestExitWorkflow()
            }
        }
    }
}

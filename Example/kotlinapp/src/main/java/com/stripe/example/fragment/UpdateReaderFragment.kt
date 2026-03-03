package com.stripe.example.fragment

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.stripe.example.MainActivity
import com.stripe.example.R
import com.stripe.example.databinding.FragmentUpdateReaderBinding
import com.stripe.example.viewmodel.UpdateReaderViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.MobileReaderListener
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate
import com.stripe.stripeterminal.external.models.TerminalException
import java.lang.ref.WeakReference

/**
 * The `UpdateReaderFragment` allows the user to check the current version of the [Reader] software,
 * as well as update it when necessary.
 */
class UpdateReaderFragment : Fragment(R.layout.fragment_update_reader), MobileReaderListener {

    companion object {
        const val TAG = "com.stripe.example.fragment.UpdateReaderFragment"
    }

    private val viewModel: UpdateReaderViewModel by viewModels()
    private lateinit var activityRef: WeakReference<MainActivity>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentUpdateReaderBinding.bind(view)

        if (viewModel.reader == null) {
            viewModel.reader = Terminal.getInstance().connectedReader
        }

        // Set initial reader info
        viewModel.reader?.let { reader ->
            binding.readerDescription.text = getString(
                R.string.reader_description,
                reader.deviceType.name,
                reader.serialNumber
            )
            binding.currentVersion.text = reader.softwareVersion
        }

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

        // Observe LiveData for UI updates
        viewModel.doneButtonVisibility.observe(viewLifecycleOwner) { visible ->
            binding.doneButton.isVisible = visible
        }

        viewModel.checkForUpdateButtonVisibility.observe(viewLifecycleOwner) { visible ->
            binding.checkForUpdateButton.isVisible = visible
        }

        viewModel.checkForUpdateButtonText.observe(viewLifecycleOwner) { textRes ->
            binding.checkForUpdateButton.text = getString(textRes)
        }

        viewModel.checkForUpdateButtonColor.observe(viewLifecycleOwner) { colorRes ->
            binding.checkForUpdateButton.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
            // Also update cancel button color based on done button visibility
            val cancelColor = if (viewModel.doneButtonVisibility.value == true) {
                R.color.colorPrimaryDark
            } else {
                R.color.colorAccent
            }
            binding.cancelButton.setTextColor(ContextCompat.getColor(requireContext(), cancelColor))
        }

        viewModel.checkForUpdateDescriptionVisibility.observe(viewLifecycleOwner) { visible ->
            binding.checkForUpdateDescription.isVisible = visible
        }

        viewModel.checkForUpdateDescriptionText.observe(viewLifecycleOwner) { text ->
            binding.checkForUpdateDescription.text = text
        }

        viewModel.hasStartedInstallingUpdate.observe(viewLifecycleOwner) { started ->
            val finished = viewModel.hasFinishedFetchingUpdate.value ?: false
            binding.installDisclaimer.isVisible = started && finished
        }

        viewModel.hasFinishedFetchingUpdate.observe(viewLifecycleOwner) { finished ->
            val started = viewModel.hasStartedInstallingUpdate.value ?: false
            binding.installDisclaimer.isVisible = started && finished
        }
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

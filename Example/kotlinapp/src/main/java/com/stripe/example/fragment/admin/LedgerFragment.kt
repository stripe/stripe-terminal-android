package com.stripe.example.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.DialogDetailedLedgerCardBinding
import com.stripe.example.databinding.FragmentLedgerBinding
import com.stripe.example.model.DetailedLedgerCard
import com.stripe.example.model.LedgerAction
import com.stripe.example.model.LedgerEntry
import com.stripe.example.viewmodel.LedgerViewModel
import java.lang.ref.WeakReference

class LedgerFragment : Fragment(R.layout.fragment_ledger) {
    private lateinit var activityRef: WeakReference<FragmentActivity?>
    private val viewModel: LedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRef = WeakReference(activity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewBinding = FragmentLedgerBinding.bind(view)
        val adapter = LedgerAdapter { entry ->
            if (viewModel.isLoading.value != true) {
                showDetailedCard(entry)
            }
        }

        viewBinding.eventRecyclerView.layoutManager = LinearLayoutManager(context)
        viewBinding.eventRecyclerView.adapter = adapter

        viewBinding.setupToolbar()
        viewBinding.observeLoadingState()

        viewModel.entries.observe(viewLifecycleOwner) { entries ->
            adapter.updateEntries(entries)
        }
    }

    private fun FragmentLedgerBinding.observeLoadingState() {
        val binding = this
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.eventRecyclerView.isEnabled = !isLoading
            binding.eventRecyclerView.suppressLayout(isLoading)
            binding.eventRecyclerView.alpha = if (isLoading) {
                0.5f
            } else {
                1.0f
            }
            binding.loadingIndicator.isVisible = isLoading
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.navigationListener = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.navigationListener = activityRef.get() as? NavigationListener
    }

    private fun FragmentLedgerBinding.setupToolbar() {
        // Find the toolbar within the AppBarLayout
        val toolbar = appBar.getChildAt(0) as? androidx.appcompat.widget.Toolbar
        toolbar?.setNavigationOnClickListener {
            (activityRef.get() as? NavigationListener)?.onRequestExitWorkflow()
        }
    }

    private fun showDetailedCard(entry: LedgerEntry) {
        val detailedCard = DetailedLedgerCard.fromLedgerEntry(entry)

        val dialogBinding = DialogDetailedLedgerCardBinding.inflate(
            LayoutInflater.from(context),
            null,
            false
        )

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Bind data to views
        dialogBinding.bindDetailedCard(detailedCard) { action ->
            handleAction(action, entry)
            dialog.dismiss()
        }

        dialog.show()

        // Make dialog almost full screen
        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun DialogDetailedLedgerCardBinding.bindDetailedCard(
        detailedCard: DetailedLedgerCard,
        onActionClick: (LedgerAction) -> Unit
    ) {
        title.text = detailedCard.title
        subtitle.text = detailedCard.subtitle
        status.text = detailedCard.status
        createdDate.text = detailedCard.createdDate

        // Amount
        if (detailedCard.amount != null) {
            amountContainer.isVisible = true
            amount.text = "\$${detailedCard.amount}"
            currency.text = detailedCard.currency ?: ""
        } else {
            amountContainer.isVisible = false
        }

        // Card details
        if (detailedCard.cardBrand != null || detailedCard.cardLast4 != null) {
            cardDetailsContainer.isVisible = true
            cardBrand.text = detailedCard.cardBrand ?: ""
            cardLast4.text = detailedCard.cardLast4?.let { "****$it" } ?: ""
        } else {
            cardDetailsContainer.isVisible = false
        }

        // Metadata
        if (detailedCard.metadata.isNotEmpty()) {
            metadataLabel.isVisible = true
            metadata.isVisible = true
            metadata.text = detailedCard.metadata.toString()
        } else {
            metadataLabel.isVisible = false
            metadata.isVisible = false
        }

        // Status icons
        cancelledIcon.isVisible = detailedCard.entry.isCancelled
        offlineIcon.isVisible = detailedCard.entry.collectedOffline
        syncIcon.isVisible = !detailedCard.entry.syncedToStripe

        // Action buttons
        val actions = detailedCard.availableActions
        captureButton.isVisible = actions.contains(LedgerAction.CAPTURE)
        captureButton.setOnClickListener { onActionClick(LedgerAction.CAPTURE) }

        refundButton.isVisible = actions.contains(LedgerAction.REFUND)
        refundButton.setOnClickListener { onActionClick(LedgerAction.REFUND) }

        cancelActionButton.isVisible = actions.contains(LedgerAction.CANCEL)
        cancelActionButton.setOnClickListener { onActionClick(LedgerAction.CANCEL) }

        refreshButton.isVisible = actions.contains(LedgerAction.REFRESH)
        refreshButton.setOnClickListener { onActionClick(LedgerAction.REFRESH) }
    }

    private fun handleAction(action: LedgerAction, entry: LedgerEntry) {
        when (action) {
            LedgerAction.CAPTURE -> {
                if (entry is LedgerEntry.Payment) {
                    viewModel.capture(entry)
                }
            }

            LedgerAction.REFUND -> {
                if (entry is LedgerEntry.Payment) {
                    viewModel.refund(entry)
                }
            }

            LedgerAction.CANCEL -> {
                viewModel.cancel(entry)
            }

            LedgerAction.REFRESH -> {
                viewModel.refresh(entry)
            }
        }
    }

    interface OnActionClickListener {
        fun onActionClick(action: LedgerAction, entry: LedgerEntry)
    }

    companion object {
        const val TAG = "com.stripe.example.fragment.LedgerFragment"
    }
}

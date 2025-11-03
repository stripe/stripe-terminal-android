package com.stripe.example.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var viewModel: LedgerViewModel
    private lateinit var binding: FragmentLedgerBinding
    private lateinit var adapter: LedgerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRef = WeakReference(activity)
        viewModel = ViewModelProvider(this)[LedgerViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_ledger,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToolbar()
        observeLoadingState()
    }

    private fun setupRecyclerView() {
        adapter = LedgerAdapter { entry ->
            if (viewModel.isLoading.value != true) {
                showDetailedCard(entry)
            }
        }

        binding.eventRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@LedgerFragment.adapter
        }
    }

    private fun observeLoadingState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.eventRecyclerView.isEnabled = !isLoading
            binding.eventRecyclerView.suppressLayout(isLoading)
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

    private fun setupToolbar() {
        // Find the toolbar within the AppBarLayout
        val toolbar = binding.appBar.getChildAt(0) as? androidx.appcompat.widget.Toolbar
        toolbar?.setNavigationOnClickListener {
            (activityRef.get() as? NavigationListener)?.onRequestExitWorkflow()
        }
    }

    private fun showDetailedCard(entry: LedgerEntry) {
        val detailedCard = DetailedLedgerCard.fromLedgerEntry(entry)

        val dialogBinding: DialogDetailedLedgerCardBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_detailed_ledger_card,
            null,
            false
        )

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.detailedCard = detailedCard
        dialogBinding.onActionClickListener = object : OnActionClickListener {
            override fun onActionClick(action: LedgerAction, entry: LedgerEntry) {
                handleAction(action, entry)
                dialog.dismiss()
            }
        }

        dialog.show()

        // Make dialog almost full screen
        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

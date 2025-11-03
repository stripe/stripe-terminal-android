package com.stripe.example.fragment.admin

import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.model.LedgerEntry

object LedgerBindingAdapter {
    @BindingAdapter("ledgerEntries")
    @JvmStatic
    fun RecyclerView.bindLedgerEntries(entries: List<LedgerEntry>?) {
        val adapter = adapter as? LedgerAdapter
        if (adapter != null && entries != null) {
            adapter.updateEntries(entries)
        }
    }
}

class LedgerAdapter(
    private val onLongClickListener: (LedgerEntry) -> Unit
) : RecyclerView.Adapter<LedgerHolder>() {

    private var entries: List<LedgerEntry> = emptyList()

    companion object {
        private const val VIEW_TYPE_PAYMENT = 1
        private const val VIEW_TYPE_CARD = 2
    }

    fun updateEntries(newEntries: List<LedgerEntry>) {
        this.entries = newEntries
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = entries.size

    override fun getItemViewType(position: Int): Int {
        return when (entries[position]) {
            is LedgerEntry.Payment -> VIEW_TYPE_PAYMENT
            is LedgerEntry.Card -> VIEW_TYPE_CARD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerHolder {
        return when (viewType) {
            VIEW_TYPE_PAYMENT -> LedgerHolder.PaymentViewHolder.create(parent)
            VIEW_TYPE_CARD -> LedgerHolder.CardViewHolder.create(parent)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: LedgerHolder, position: Int) {
        holder.bind(entries[position], onLongClickListener)
    }
}

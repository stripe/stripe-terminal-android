package com.stripe.example.fragment.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.databinding.ListItemLedgerCardBinding
import com.stripe.example.databinding.ListItemLedgerPaymentBinding
import com.stripe.example.fragment.admin.LedgerHolder.CardViewHolder
import com.stripe.example.fragment.admin.LedgerHolder.PaymentViewHolder
import com.stripe.example.model.LedgerEntry

class LedgerAdapter(
    private val onLongClickListener: (LedgerEntry) -> Unit
) : RecyclerView.Adapter<LedgerHolder>() {
    private val differ = AsyncListDiffer(this, ItemCallback())

    companion object {
        private const val VIEW_TYPE_PAYMENT = 1
        private const val VIEW_TYPE_CARD = 2
    }

    fun updateEntries(newEntries: List<LedgerEntry>) {
        differ.submitList(newEntries)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is LedgerEntry.Payment -> VIEW_TYPE_PAYMENT
            is LedgerEntry.Card -> VIEW_TYPE_CARD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerHolder {
        return when (viewType) {
            VIEW_TYPE_PAYMENT -> {
                PaymentViewHolder(
                    ListItemLedgerPaymentBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            VIEW_TYPE_CARD -> {
                CardViewHolder(
                    ListItemLedgerCardBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> error("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: LedgerHolder, position: Int) {
        holder.bind(differ.currentList[position], onLongClickListener)
    }

    private class ItemCallback : DiffUtil.ItemCallback<LedgerEntry>() {
        override fun areItemsTheSame(oldItem: LedgerEntry, newItem: LedgerEntry): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: LedgerEntry, newItem: LedgerEntry): Boolean {
            return oldItem == newItem
        }
    }
}

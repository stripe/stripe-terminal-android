package com.stripe.example.fragment.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.R
import com.stripe.example.databinding.ListItemLedgerCardBinding
import com.stripe.example.databinding.ListItemLedgerPaymentBinding
import com.stripe.example.model.LedgerEntry

sealed class LedgerHolder(
    view: View,
) : RecyclerView.ViewHolder(view) {
    abstract fun bind(entry: LedgerEntry, onLongClickListener: (LedgerEntry) -> Unit)

    class PaymentViewHolder(
        private val binding: ListItemLedgerPaymentBinding
    ) : LedgerHolder(binding.root) {
        override fun bind(entry: LedgerEntry, onLongClickListener: (LedgerEntry) -> Unit) {
            if (entry is LedgerEntry.Payment) {
                binding.entry = entry
                binding.onLongClickListener = View.OnLongClickListener {
                    onLongClickListener(entry)
                    true
                }
                binding.executePendingBindings()
            }
        }

        companion object {
            fun create(parent: ViewGroup): PaymentViewHolder {
                val binding: ListItemLedgerPaymentBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.list_item_ledger_payment,
                    parent,
                    false
                )
                return PaymentViewHolder(binding)
            }
        }
    }

    class CardViewHolder(
        private val binding: ListItemLedgerCardBinding
    ) : LedgerHolder(binding.root) {
        override fun bind(entry: LedgerEntry, onLongClickListener: (LedgerEntry) -> Unit) {
            if (entry is LedgerEntry.Card) {
                binding.entry = entry
                binding.onLongClickListener = View.OnLongClickListener {
                    onLongClickListener(entry)
                    true
                }
                binding.executePendingBindings()
            }
        }

        companion object {
            fun create(parent: ViewGroup): CardViewHolder {
                val binding: ListItemLedgerCardBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.list_item_ledger_card,
                    parent,
                    false
                )
                return CardViewHolder(binding)
            }
        }
    }
}

package com.stripe.example.fragment.admin

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.R
import com.stripe.example.databinding.ListItemLedgerCardBinding
import com.stripe.example.databinding.ListItemLedgerPaymentBinding
import com.stripe.example.model.LedgerEntry
import java.util.Locale

sealed class LedgerHolder(
    view: View,
) : RecyclerView.ViewHolder(view) {
    abstract fun bind(entry: LedgerEntry, onLongClickListener: (LedgerEntry) -> Unit)

    class PaymentViewHolder(
        private val binding: ListItemLedgerPaymentBinding
    ) : LedgerHolder(binding.root) {
        override fun bind(entry: LedgerEntry, onLongClickListener: (LedgerEntry) -> Unit) {
            if (entry is LedgerEntry.Payment) {
                binding.cardView.setOnLongClickListener {
                    onLongClickListener(entry)
                    true
                }

                binding.cancelledIcon.isVisible = entry.isCancelled
                binding.offlineIcon.isVisible = entry.collectedOffline
                binding.syncIcon.isVisible = !entry.syncedToStripe
                binding.capturableIcon.isVisible = entry.isCapturable
                binding.refundedIcon.isVisible = entry.refunded

                binding.amountText.text = itemView.resources.getString(
                    R.string.formatted_amount,
                    entry.formattedAmount,
                )
                binding.currencyText.text = entry.intent.currency?.uppercase(Locale.getDefault()).orEmpty()
                binding.metadataText.text = entry.intent.metadata.toString()
                binding.dateText.text = entry.createdDate
            }
        }
    }

    class CardViewHolder(
        private val binding: ListItemLedgerCardBinding
    ) : LedgerHolder(binding.root) {
        override fun bind(entry: LedgerEntry, onLongClickListener: (LedgerEntry) -> Unit) {
            if (entry is LedgerEntry.Card) {
                binding.cardView.setOnLongClickListener {
                    onLongClickListener(entry)
                    true
                }

                binding.cancelledIcon.isVisible = entry.isCancelled
                binding.offlineIcon.isVisible = entry.collectedOffline
                binding.syncIcon.isVisible = !entry.syncedToStripe

                val paymentMethod = entry.intent.paymentMethod
                binding.brandText.text = paymentMethod?.cardPresentDetails?.brand.orEmpty()
                binding.last4Text.text = paymentMethod?.let { "****${it.cardPresentDetails?.last4}" }.orEmpty()
                binding.metadataText.text = entry.intent.metadata.toString()
                binding.dateText.text = entry.createdDate
            }
        }
    }
}

package com.stripe.example.model

import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentStatus
import com.stripe.stripeterminal.external.models.Refund
import com.stripe.stripeterminal.external.models.SetupIntent
import com.stripe.stripeterminal.external.models.SetupIntentStatus
import java.util.Date

sealed interface LedgerEntry {
    /**
     * Whether the entry has been cancelled
     */
    val isCancelled: Boolean

    /**
     * Whether the entry has been successfully synced to Stripe
     */
    val syncedToStripe: Boolean

    /**
     * Whether the entry was collected offline
     */
    val collectedOffline: Boolean

    /**
     * Whether the entry can be cancelled
     */
    val isCancellable: Boolean
        get() = !isCancelled && syncedToStripe

    val createdDate: String

    val uniqueId: String

    data class Payment(
        val intent: PaymentIntent,
        val refund: Refund? = null,
    ) : LedgerEntry {
        override val isCancelled: Boolean = intent.status == PaymentIntentStatus.CANCELED
        override val syncedToStripe: Boolean = intent.offlineDetails?.requiresUpload != true
        override val collectedOffline: Boolean = intent.offlineDetails != null
        val isCapturable: Boolean =
            intent.run { status == PaymentIntentStatus.REQUIRES_CAPTURE && offlineDetails?.requiresUpload != true }
        val isRefundable: Boolean =
            refund == null &&
                intent.run { status == PaymentIntentStatus.SUCCEEDED || isCapturable }
        val refunded: Boolean = refund != null

        override val createdDate: String
            get() = java.text.DateFormat.getDateTimeInstance().format(Date(intent.created * 1000))
        val formattedAmount: String = String.format("%.2f", intent.amount / 100.0)
        override val uniqueId: String = intent.metadata?.get("transaction_id") ?: intent.id ?: ""
    }

    data class Card(
        val intent: SetupIntent
    ) : LedgerEntry {
        override val isCancelled: Boolean = intent.status == SetupIntentStatus.CANCELLED
        override val syncedToStripe: Boolean = intent.offlineDetails?.requiresUpload != true
        override val collectedOffline: Boolean = intent.offlineDetails != null
        override val createdDate: String
            get() = java.text.DateFormat.getDateTimeInstance().format(Date(intent.created * 1000))
        override val uniqueId: String = intent.metadata["transaction_id"] ?: intent.id ?: ""
    }
}

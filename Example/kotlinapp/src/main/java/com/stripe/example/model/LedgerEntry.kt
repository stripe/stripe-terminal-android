package com.stripe.example.model

import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentStatus
import com.stripe.stripeterminal.external.models.Refund
import com.stripe.stripeterminal.external.models.SetupIntent
import com.stripe.stripeterminal.external.models.SetupIntentStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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
            get() = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .format(Instant.ofEpochSecond(intent.created).atZone(ZoneId.systemDefault()))
        val formattedAmount: String = String.format(Locale.US, "%.2f", intent.amount / 100.0)
        override val uniqueId: String = intent.metadata?.get("transaction_id") ?: intent.id ?: ""
    }

    data class Card(
        val intent: SetupIntent
    ) : LedgerEntry {
        override val isCancelled: Boolean = intent.status == SetupIntentStatus.CANCELLED
        override val syncedToStripe: Boolean = intent.offlineDetails?.requiresUpload != true
        override val collectedOffline: Boolean = intent.offlineDetails != null
        override val createdDate: String
            get() = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .format(Instant.ofEpochSecond(intent.created).atZone(ZoneId.systemDefault()))
        override val uniqueId: String = intent.metadata["transaction_id"] ?: intent.id ?: ""
    }
}

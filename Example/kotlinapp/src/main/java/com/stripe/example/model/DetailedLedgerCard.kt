package com.stripe.example.model

/**
 * Data class representing the detailed view of a ledger entry
 * Used for the exploded card dialog that appears on long press
 */
data class DetailedLedgerCard(
    val entry: LedgerEntry,
    val title: String,
    val subtitle: String,
    val amount: String?,
    val currency: String?,
    val cardBrand: String?,
    val cardLast4: String?,
    val metadata: Map<String, String>,
    val createdDate: String,
    val status: String,
    val availableActions: List<LedgerAction>
) {
    companion object {
        fun fromLedgerEntry(entry: LedgerEntry): DetailedLedgerCard {
            return when (entry) {
                is LedgerEntry.Payment -> {
                    val intent = entry.intent
                    val actions = mutableListOf<LedgerAction>(LedgerAction.REFRESH)

                    // Add available actions based on entry state
                    if (entry.isCapturable) {
                        actions.add(LedgerAction.CAPTURE)
                    }
                    if (entry.isRefundable) {
                        actions.add(LedgerAction.REFUND)
                    }
                    if (entry.isCancellable) {
                        actions.add(LedgerAction.CANCEL)
                    }

                    DetailedLedgerCard(
                        entry = entry,
                        title = "Payment Intent",
                        subtitle = intent.id ?: "Unknown ID",
                        amount = String.format("%.2f", intent.amount / 100.0),
                        currency = intent.currency?.uppercase(),
                        cardBrand = intent.getCharges().firstOrNull()?.paymentMethodDetails?.cardPresentDetails?.brand,
                        cardLast4 = intent.getCharges().firstOrNull()?.paymentMethodDetails?.cardPresentDetails?.last4,
                        metadata = intent.metadata ?: emptyMap(),
                        createdDate = java.text.DateFormat.getDateTimeInstance()
                            .format(java.util.Date(intent.created * 1000)),
                        status = intent.status?.name ?: "Unknown",
                        availableActions = actions
                    )
                }
                is LedgerEntry.Card -> {
                    val intent = entry.intent
                    val actions = mutableListOf<LedgerAction>(LedgerAction.REFRESH)

                    // Add available actions based on entry state
                    if (entry.isCancellable) {
                        actions.add(LedgerAction.CANCEL)
                    }

                    DetailedLedgerCard(
                        entry = entry,
                        title = "Setup Intent",
                        subtitle = intent.id ?: "Unknown ID",
                        amount = null,
                        currency = null,
                        cardBrand = intent.paymentMethod?.cardPresentDetails?.brand,
                        cardLast4 = intent.paymentMethod?.cardPresentDetails?.last4,
                        metadata = intent.metadata,
                        createdDate = java.text.DateFormat.getDateTimeInstance()
                            .format(java.util.Date(intent.created * 1000)),
                        status = intent.status?.name ?: "Unknown",
                        availableActions = actions
                    )
                }
            }
        }
    }
}

/**
 * Available actions for ledger entries
 */
enum class LedgerAction(val displayName: String) {
    CAPTURE("Capture"),
    REFUND("Refund"),
    CANCEL("Cancel"),
    REFRESH("Refresh Status")
}

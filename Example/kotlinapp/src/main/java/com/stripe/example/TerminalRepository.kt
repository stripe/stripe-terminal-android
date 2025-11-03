package com.stripe.example

import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.Refund
import com.stripe.stripeterminal.external.models.SetupIntent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * A repository for managing Terminal SDK payments & set up intents.
 */
object TerminalRepository {
    const val TRANSACTION_ID_KEY = "transaction_id"
    val paymentIntents: Flow<List<PaymentIntent>>
        get() = _paymentIntents.map { it.values.toList() }
    val setupIntents: Flow<List<SetupIntent>>
        get() = _setupIntents.map { it.values.toList() }
    val refunds: Flow<List<Refund>>
        get() = _refunds

    private val _paymentIntents: MutableStateFlow<Map<String, PaymentIntent>> = MutableStateFlow(emptyMap())
    private val _setupIntents: MutableStateFlow<Map<String, SetupIntent>> = MutableStateFlow(emptyMap())
    private val _refunds: MutableStateFlow<List<Refund>> = MutableStateFlow(emptyList())

    fun addPaymentIntent(paymentIntent: PaymentIntent) {
        _paymentIntents.update { currentMap ->
            currentMap + paymentIntent.let { it.transactionId to it }
        }
    }

    fun addSetupIntent(setupIntent: SetupIntent) {
        _setupIntents.update { currentMap -> currentMap + setupIntent.let { it.transactionId to it } }
    }

    fun addRefund(refund: Refund) {
        _refunds.update { currentList -> currentList + refund }
    }

    // Using a map to prevent duplicates, keyed by transactionId
    private val PaymentIntent.transactionId: String
        get() = requireNotNull(metadata?.get(TRANSACTION_ID_KEY) ?: id) // fallback to id if metadata is missing

    // Using a map to prevent duplicates, keyed by transactionId
    private val SetupIntent.transactionId: String
        get() = requireNotNull(metadata[TRANSACTION_ID_KEY] ?: id) // fallback to id if metadata is missing

    fun getPaymentIntentByTransactionId(transactionId: String): PaymentIntent? {
        return _paymentIntents.value[transactionId]
    }

    fun getSetupIntentByTransactionId(transactionId: String): SetupIntent? {
        return _setupIntents.value[transactionId]
    }

    /**
     * Generate metadata to attach to PaymentIntents, SetupIntents & Refunds created by the app.
     */
    fun genMetaData(): Map<String, String> {
        return mapOf(
            "store_id" to "store_1234",
            "register_id" to "register_1",
            TRANSACTION_ID_KEY to "${UUID.randomUUID()}"
        )
    }
}

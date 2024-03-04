package com.stripe.example.model

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.stripe.stripeterminal.external.InternalApi
import com.stripe.stripeterminal.external.OfflineMode
import com.stripe.stripeterminal.external.models.NetworkStatus
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.TerminalException
import java.util.Date

@OptIn(OfflineMode::class)
sealed interface OfflineEvent {
    val summary: String
    val details: List<Pair<String, String>>
    val timestamp: Date

    abstract class ChangeEvent<T : Any?>(
        event: String,
        previous: T,
        new: T,
    ) : OfflineEvent {
        final override val timestamp: Date = Date(System.currentTimeMillis())
        override val summary: String = "$timestamp : $event Change from $previous to $new"
        override val details: List<Pair<String, String>> = emptyList()
        val changed = previous != new
    }

    data class ConnectivityChange(
        val previous: NetworkStatus,
        val new: NetworkStatus,
        val connectionType: ConnectionType,
    ) : ChangeEvent<NetworkStatus>("Connectivity (${connectionType.type})", previous, new)

    class ForwardingEvent(
        summary: String,
        details: List<Pair<String, String>>,
        val isSuccessFul: Boolean,
    ) : OfflineEvent {
        override val timestamp = Date(System.currentTimeMillis())
        override val summary = "$timestamp : $summary"
        override val details = details.plus(TIME to timestamp.toString())

        constructor(error: TerminalException) : this(
            summary = "Fatal error forwarding offline payments",
            details = listOf(ERROR to beautify(error)),
            isSuccessFul = false
        )

        constructor(paymentIntent: PaymentIntent, error: TerminalException?) : this(
            summary = when (error) {
                null -> "Forwarded ${paymentIntent.run { "${identifiers()}, $amount $currency" }}"
                else -> "Failed to forward ${paymentIntent.identifiers()}"
            },
            details = mutableListOf(PAYMENT_INTENT to beautify(paymentIntent))
                .apply {
                    paymentIntent.offlineDetails?.let { }
                    paymentIntent.offlineDetails?.let { add(OFFLINE_DETAILS to beautify(it)) }
                    error?.let { add(ERROR to beautify(it)) }
                },
            isSuccessFul = error == null
        )
    }

    class CaptureEvent(summary: String, details: List<Pair<String, String>>) : OfflineEvent {
        override val timestamp = Date(System.currentTimeMillis())
        override val summary = "$timestamp : $summary"
        override val details = details.apply {
            if (isNotEmpty()) {
                plus(TIME to timestamp.toString())
            }
        }

        constructor(paymentIntent: PaymentIntent, error: TerminalException?) : this(
            summary = when (error) {
                null -> "Captured ${paymentIntent.run { "${identifiers()}, $amount $currency" }}"
                else -> "Failed to capture ${paymentIntent.identifiers()}"
            },
            details = when (error) {
                null -> emptyList()
                else -> mutableListOf(ERROR to beautify(error))
            }
        )
    }

    companion object {
        private const val ERROR = "ERROR"
        private const val PAYMENT_INTENT = "PAYMENT_INTENT"
        private const val OFFLINE_DETAILS = "OFFLINE_DETAILS"
        private const val TIME = "TIME"

        private fun beautify(to: Any): String {
            return runCatching { PRETTY_PRINT_GSON.toJson(to) }
                .getOrDefault(
                    defaultValue = when (to) {
                        is Throwable -> to.stackTraceToString()
                        else -> to.toString()
                    }
                )
        }

        private val PRETTY_PRINT_GSON = GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        @OptIn(InternalApi::class)
        fun PaymentIntent.identifiers(): String {
            return "$id <> ${offlineDetails?.id}"
        }
    }
}

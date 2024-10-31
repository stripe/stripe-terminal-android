package com.stripe.example

import com.stripe.example.model.ConnectionType
import com.stripe.example.model.OfflineEvent
import com.stripe.example.model.OfflineForwardingSummary
import com.stripe.example.network.ApiClient
import com.stripe.stripeterminal.external.OfflineMode
import com.stripe.stripeterminal.external.callable.OfflineListener
import com.stripe.stripeterminal.external.models.NetworkStatus
import com.stripe.stripeterminal.external.models.OfflineStatus
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentStatus
import com.stripe.stripeterminal.external.models.TerminalErrorCode
import com.stripe.stripeterminal.external.models.TerminalException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext

@OptIn(OfflineMode::class, FlowPreview::class)
object TerminalOfflineListener : OfflineListener {
    private val previousNetworkStatusByConnectionType: MutableMap<String, NetworkStatus> =
            mutableMapOf(
                    ConnectionType.SDK.name to NetworkStatus.UNKNOWN,
                    ConnectionType.SMART_READER.name to NetworkStatus.UNKNOWN
            )
    private val _status = MutableStateFlow(NetworkStatus.UNKNOWN)
    private val _events = MutableStateFlow<List<OfflineEvent>>(emptyList())
    private val _paymentIntentsToCapture = MutableSharedFlow<PaymentIntent>(extraBufferCapacity = 5)
    private val lastShownSummaryEventTimestamp = MutableStateFlow<Long>(0L)

    private val captureEvents = _paymentIntentsToCapture.map { paymentIntent ->
        withContext(Dispatchers.IO) {
            try {
                paymentIntent.id?.let { intentId -> ApiClient.capturePaymentIntent(intentId) }
                        ?: throw IllegalStateException("Capturable payment intents should always have a defined ID.")
                OfflineEvent.CaptureEvent(paymentIntent, null)
            } catch (error: Throwable) {
                OfflineEvent.CaptureEvent(
                        paymentIntent,
                        when (error) {
                            is TerminalException -> error
                            else -> TerminalException(
                                    errorCode = TerminalErrorCode.UNEXPECTED_SDK_ERROR,
                                    cause = error,
                                    errorMessage = "Failed to capture payment intent."
                            )
                        }
                )
            }
        }
    }
            .runningFold(emptyList<OfflineEvent>()) { list, event -> list + event }
            // Replay the last single event, so we don't block any `combine` transformations.
            .shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, replay = 1)

    val offlineEventsFlow = combine(captureEvents, _events) { captureEvents, offlineEvents ->
        (offlineEvents + captureEvents).sortedBy { it.timestamp }
    }

    val offlineStatus = _status

    /**
     * Exposes a summary of successful and failed forwards
     */
    val offlineForwardingSummaryFlow: Flow<OfflineForwardingSummary> =
            combine(offlineEventsFlow, lastShownSummaryEventTimestamp) { events, timestamp ->
                events
                        .filterIsInstance<OfflineEvent.ForwardingEvent>() // filter only forwarding events
                        .filter { it.timestamp.time >= timestamp } // exclude events we've shown a summary
                        .groupBy { it.isSuccessFul }
                        .let { map ->
                            // Aggregate results by successful flag
                            OfflineForwardingSummary(
                                    successful = map[true].orEmpty().size,
                                    failed = map[false].orEmpty().size
                            )
                        }
            }
                    // Do not emit when there are no forwarding results.
                    .filterNot { it.hasNoEvents }
                    // Debounce emissions by 10 seconds to reduce trashing
                    .debounce(timeoutMillis = 10_000L)

    override fun onOfflineStatusChange(offlineStatus: OfflineStatus) {
        _status.tryEmit(offlineStatus.sdk.networkStatus)
        listOf(ConnectionType.SDK, ConnectionType.SMART_READER).forEach { connectionType ->
            val currentNetworkStatus = when (connectionType) {
                ConnectionType.SDK -> {
                    offlineStatus.sdk.networkStatus
                }
                ConnectionType.SMART_READER -> {
                    offlineStatus.reader?.networkStatus ?: NetworkStatus.UNKNOWN
                }
            }

            val previousNetworkStatus = previousNetworkStatusByConnectionType[connectionType.type] ?: NetworkStatus.UNKNOWN
            if (previousNetworkStatus != currentNetworkStatus) {
                _events.run {
                    value =
                            value + OfflineEvent.ConnectivityChange(
                                    previousNetworkStatus,
                                    currentNetworkStatus,
                                    connectionType
                            )
                }
                previousNetworkStatusByConnectionType[connectionType.type] = currentNetworkStatus
            }
        }
    }

    override fun onForwardingFailure(e: TerminalException) {
        _events.run { value = value + OfflineEvent.ForwardingEvent(e) }
    }

    override fun onPaymentIntentForwarded(paymentIntent: PaymentIntent, e: TerminalException?) {
        _events.run { value = value + OfflineEvent.ForwardingEvent(paymentIntent, e) }
        if (paymentIntent.isCapturable()) {
            _paymentIntentsToCapture.tryEmit(paymentIntent)
        }
    }

    private fun PaymentIntent.isCapturable(): Boolean {
        return status == PaymentIntentStatus.REQUIRES_CAPTURE && !(offlineDetails?.requiresUpload ?: false)
    }
}

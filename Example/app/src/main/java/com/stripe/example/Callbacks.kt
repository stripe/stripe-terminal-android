package com.stripe.example

import com.stripe.stripeterminal.*

/**
 * A [ReaderCallback] that notifies the [TerminalStateManager] that connection has completed
 */
class ConnectionCallback(private val manager: TerminalStateManager) : ReaderCallback {

    override fun onSuccess(reader: Reader?) {
        manager.onConnectReader()
    }

    override fun onFailure(e: TerminalException) {
        manager.onFailure(e)
    }
}

/**
 * A [PaymentIntentCallback] that notifies the [TerminalStateManager] that [PaymentIntent] creation
 * has completed
 */
class CreatePaymentIntentCallback(private val manager: TerminalStateManager): PaymentIntentCallback {
    override fun onSuccess(paymentIntent: PaymentIntent) {
        manager.onCreatePaymentIntent(paymentIntent)
    }

    override fun onFailure(e: TerminalException) {
        manager.onFailure(e)
    }
}

/**
 * A [PaymentIntentCallback] that notifies the [TerminalStateManager] that payment method collection
 * has completed
 */
class CollectPaymentMethodCallback(private val manager: TerminalStateManager): PaymentIntentCallback {
    override fun onSuccess(paymentIntent: PaymentIntent) {
        manager.onCollectPaymentMethod(paymentIntent)
    }

    override fun onFailure(e: TerminalException) {
        manager.onFailure(e)
    }
}

/**
 * A [PaymentIntentCallback] that notifies the [TerminalStateManager] that [PaymentIntent]
 * confirmation has completed
 */
class ConfirmPaymentIntentCallback(private val manager: TerminalStateManager): PaymentIntentCallback {
    override fun onSuccess(paymentIntent: PaymentIntent) {
        manager.onConfirmPaymentIntent(paymentIntent)
    }

    override fun onFailure(e: TerminalException) {
        manager.onFailure(e)
    }
}

/**
 * A [Callback] that notifies the [TerminalStateManager] when disconnect has completed
 */
class DisconnectCallback(private val manager: TerminalStateManager) : Callback {
    override fun onSuccess() {
        manager.onDisconnectReader()
    }

    override fun onFailure(e: TerminalException) {
        manager.onFailure(e)
    }
}

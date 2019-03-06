package com.stripe.example

import com.stripe.stripeterminal.PaymentIntent
import com.stripe.stripeterminal.Reader
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.TerminalException

/**
 * An `Activity` that should be notified when various [Terminal] actions have completed
 */
interface TerminalStateManager {

    /**
     * Notify the `Activity` that a payment method has been collected
     */
    fun onCollectPaymentMethod(paymentIntent: PaymentIntent)

    /**
     * Notify the `Activity` that a [PaymentIntent] has been confirmed
     */
    fun onConfirmPaymentIntent(paymentIntent: PaymentIntent)

    /**
     * Notify the `Activity` that a [Reader] has been connected to
     */
    fun onConnectReader()

    /**
     * Notify the `Activity` that a [PaymentIntent] has been created
     */
    fun onCreatePaymentIntent(paymentIntent: PaymentIntent)

    /**
     * Notify the `Activity` that we've disconnected from all [Reader]s
     */
    fun onDisconnectReader()

    /**
     * Notify the `Activity` that a [TerminalException] has been thrown
     */
    fun onFailure(e: TerminalException)

}
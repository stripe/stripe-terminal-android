package com.stripe.example

import com.stripe.stripeterminal.*

/**
 * An `Activity` that should be notified when various [Terminal] actions have completed
 */
interface TerminalStateManager {

    /**
     * Notify the `Activity` that collecting payment method has been canceled
     */
    fun onCancelCollectPaymentMethod()

    /**
     * Notify the `Activity` that discovery has been canceled
     */
    fun onCancelDiscovery()

    /**
     * Notify the `Activity` that a payment method has been collected
     */
    fun onCollectPaymentMethod(paymentIntent: PaymentIntent)

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
     * Notify the `Activity` that [Reader] discovery has completed
     */
    fun onDiscoverReaders()

    /**
     * Notify the `Activity` that a [TerminalException] has been thrown
     */
    fun onFailure(e: TerminalException)

    /**
     * Notify the `Activity` that a reader software update has been installed
     */
    fun onInstallReaderSoftwareUpdate()

    /**
     * Notify the `Activity` that the payment has been processed
     */
    fun onProcessPayment(paymentIntent: PaymentIntent)

    /**
     * Notify the `Activity` that the payment method has been created
     */
    fun onReadReusableCard(paymentMethod: PaymentMethod)

    /**
     * Notify the `Activity` that a reader software update has been found
     */
    fun onReturnReaderSoftwareUpdate(update: ReaderSoftwareUpdate?)

}
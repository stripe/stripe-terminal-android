package com.stripe.example

import com.stripe.stripeterminal.Reader

/**
 * An `Activity` that should be notified when various navigation activities have been triggered
 */
interface NavigationListener {

    /**
     * Notify the `Activity` that collecting a payment method should be canceled
     */
    fun onRequestCancelCollectPaymentMethod()

    /**
     * Notify the `Activity` that discovery should be canceled
     */
    fun onRequestCancelDiscovery()

    /**
     * Notify the `Activity` that disconnect has been requested
     */
    fun onRequestDisconnect()

    /**
     * Notify the `Activity` that discovery should begin
     */
    fun onRequestDiscovery()

    /**
     * Notify the `Activity` that the user wants to exit the payment workflow
     */
    fun onRequestExitPaymentWorkflow()

    /**
     * Notify the `Activity` that the user wants to initiate a payment
     */
    fun onRequestPayment(amount: Int, currency: String)

    /**
     * Notify the `Activity` that the user wants to start the payment workflow
     */
    fun onSelectPaymentWorkflow()

    /**
     * Notify the `Activity` that a [Reader] has been selected
     */
    fun onSelectReader(reader: Reader)

    /**
     * Notify the `Activity` that the simulated switch has been toggled
     */
    fun onToggleSimulatedSwitch(isOn: Boolean)
}
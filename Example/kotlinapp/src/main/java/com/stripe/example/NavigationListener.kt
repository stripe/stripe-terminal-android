package com.stripe.example

import com.stripe.stripeterminal.external.models.DiscoveryMethod
import com.stripe.stripeterminal.external.models.Reader

/**
 * An `Activity` that should be notified when various navigation activities have been triggered
 */
interface NavigationListener {
    /**
     * Notify the `Activity` that collecting payment method has been canceled
     */
    fun onCancelCollectPaymentMethod()

    /**
     * Notify the `Activity` that discovery should begin
     */
    fun onRequestDiscovery(isSimulated: Boolean, discoveryMethod: DiscoveryMethod)

    /**
     * Notify the `Activity` that discovery has been canceled
     */
    fun onCancelDiscovery()

    /**
     * Notify the `Activity` that the [Reader] has been disconnected
     */
    fun onDisconnectReader()

    /**
     * Notify the `Activity` that the user wants to exit the current workflow
     */
    fun onRequestExitWorkflow()

    /**
     * Notify the `Activity` that the user wants to initiate a payment
     */
    fun onRequestPayment(
        amount: Long,
        currency: String,
        skipTipping: Boolean,
        extendedAuth: Boolean,
        incrementalAuth: Boolean
    )

    /**
     * Notify the `Activity` that a [Reader] has been connected
     */
    fun onConnectReader()

    /**
     * Notify the `Activity` that the user wants to start the payment workflow
     */
    fun onSelectPaymentWorkflow()

    /**
     * Notify the `Activity` that the user wants to start the workflow to read a reusable card
     */
    fun onSelectReadReusableCardWorkflow()

    /**
     * Notify the `Activity` that the user wants to start the update reader workflow
     */
    fun onSelectUpdateWorkflow()

    /**
     * Notify the `Activity` that the user has requested to change the location.
     */
    fun onRequestChangeLocation()

    /**
     * Notify the `Activity` that the user has requested to add a location.
     */
    fun onRequestCreateLocation()

    /**
     * Notify the `Activity` that the user has finished creating a location.
     */
    fun onLocationCreated()
}

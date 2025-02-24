package com.stripe.example

import com.stripe.example.fragment.discovery.DiscoveryMethod
import com.stripe.example.model.OfflineBehaviorSelection
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
     * Notify the `Activity` that collecting setup intent has been canceled
     */
    fun onCancelCollectSetupIntent()

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
        incrementalAuth: Boolean,
        offlineBehaviorSelection: OfflineBehaviorSelection,
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
     * Notify the `Activity` that the user wants to start the workflow to save a card
     */
    fun onRequestSaveCard()

    /**
     * Notify the `Activity` that the user wants to start the update reader workflow
     */
    fun onSelectUpdateWorkflow()

    /**
     * Notify the `Activity` that the user wants to view the offline logs
     */
    fun onSelectViewOfflineLogs()

    /**
     * Notify the `Activity` that the user has requested to change the location.
     */
    fun onRequestLocationSelection()

    /**
     * Notify the `Activity` that the location selection flow has been canceled.
     */
    fun onCancelLocationSelection()

    /**
     * Notify the `Activity` that the user has requested to add a location.
     */
    fun onRequestCreateLocation()

    /**
     * Notify the `Activity` that the create location flow has been canceled.
     */
    fun onCancelCreateLocation()

    /**
     * Notify the `Activity` that the user has finished creating a location.
     */
    fun onLocationCreated()
}

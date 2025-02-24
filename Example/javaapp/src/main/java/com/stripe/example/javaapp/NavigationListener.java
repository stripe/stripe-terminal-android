package com.stripe.example.javaapp;

import com.stripe.example.javaapp.fragment.discovery.DiscoveryMethod;
import com.stripe.example.javaapp.model.OfflineBehaviorSelection;

import org.jetbrains.annotations.NotNull;

/**
 * An `Activity` that should be notified when various navigation activities have been triggered
 */
public interface NavigationListener {
    /**
     * Notify the `Activity` that collecting payment method has been canceled
     */
    void onCancelCollectPaymentMethod();

    /**
     * Notify the `Activity` that collecting setup intent has been canceled
     */
    void onCancelCollectSetupIntent();

    /**
     * Notify the `Activity` that discovery should begin
     */
    void onRequestDiscovery(boolean isSimulated, DiscoveryMethod discoveryMethod);

    /**
     * Notify the `Activity` that discovery has been canceled
     */
    void onCancelDiscovery();

    /**
     * Notify the `Activity` that the [Reader] has been disconnected
     */
    void onDisconnectReader();

    /**
     * Notify the `Activity` that the user wants to exit the current workflow
     */
    void onRequestExitWorkflow();

    /**
     * Notify the `Activity` that the user wants to initiate a payment
     */
    void onRequestPayment(long amount, @NotNull String currency, boolean skipTipping, boolean extendedAuth, boolean incrementalAuth, OfflineBehaviorSelection offlineBehaviorSelection);

    /**
     * Notify the `Activity` that a [Reader] has been connected
     */
    void onConnectReader();

    /**
     * Notify the `Activity` that the user wants to start the payment workflow
     */
    void onSelectPaymentWorkflow();

    /**
     * Notify the `Activity` that the user wants to start the workflow to save a card
     */
    void onRequestSaveCard();

    /**
     * Notify the `Activity` that the user wants to start the update reader workflow
     */
    void onSelectUpdateWorkflow();

    /**
     * Notify the `Activity` that the user wants to view the offline logs
     */
    void onSelectViewOfflineLogs();

    /**
     * Notify the `Activity` that the user has requested to change the location.
     */
    void onRequestLocationSelection();

    /**
     * Notify the `Activity` that the location selection flow has been canceled.
     */
    void onCancelLocationSelection();

    /**
     * Notify the `Activity` that the user has requested to add a location.
     */
    void onRequestCreateLocation();

    /**
     * Notify the `Activity` that the create location flow has been canceled.
     */
    void onCancelCreateLocation();

    /**
     * Notify the `Activity` that the user has finished creating a location.
     */
    void onLocationCreated();
}

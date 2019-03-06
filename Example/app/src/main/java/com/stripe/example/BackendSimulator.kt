package com.stripe.example

import com.stripe.Stripe
import com.stripe.exception.StripeException
import com.stripe.model.PaymentIntent
import com.stripe.model.terminal.ConnectionToken

/**
 * The `BackendSimulator` has been included as part of the example app's client-side code for
 * demonstration purposes only. Secret API keys should be kept confidential and stored only on your
 * own servers. Your account's secret API key can perform any API request to Stripe without
 * restriction. You should NEVER hardcode your secret API key into your app, and only call these
 * API endpoints from your backend.
 *
 * @see [Keys](https://stripe.com/docs/keys)
 */
object BackendSimulator {

    private const val API_KEY = "sk_test_032NCVVTLNB9UJ63lQcD29ZK"

    /**
     * Create a connection token with the Stripe Java bindings
     * @return The string form of the token
     * @throws StripeException
     */
    @Throws(StripeException::class)
    internal fun createConnectionToken(): String {
        Stripe.apiKey = API_KEY
        val params = emptyMap<String, Any>()
        return ConnectionToken.create(params).secret
    }

    /**
     * Capture a confirmed [PaymentIntent] with the Stripe Java bindings
     * @param paymentIntentId The ID of the intent that should be captured
     */
    @Throws(StripeException::class)
    internal fun capturePaymentIntent(paymentIntentId: String) {
        PaymentIntent.retrieve(paymentIntentId).capture()
    }
}

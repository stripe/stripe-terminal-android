package com.stripe.example.network

import com.stripe.example.model.ConnectionToken
import com.stripe.example.model.PaymentIntentCreationResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * The `BackendService` interface handles the two simple calls we need to make to our backend.
 */
interface BackendService {

    /**
     * Get a connection token string from the backend
     */
    @POST("connection_token")
    fun getConnectionToken(): Call<ConnectionToken>

    /**
     * Capture a specific payment intent on our backend
     */
    @FormUrlEncoded
    @POST("capture_payment_intent")
    fun capturePaymentIntent(@Field("payment_intent_id") id: String): Call<Void>

    /**
     * Create a PaymentIntent in example backend and return PaymentIntentCreationResponse
     * For internet readers, you need to create paymentIntent in backend
     * https://stripe.com/docs/terminal/payments/collect-payment?terminal-sdk-platform=android#create-payment
     */
    @FormUrlEncoded
    @POST("create_payment_intent")
    fun createPaymentIntent(
        @FieldMap createPaymentIntentParams: Map<String, String>
    ): Call<PaymentIntentCreationResponse>
}

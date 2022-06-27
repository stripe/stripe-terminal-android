package com.stripe.example.javaapp.network;

import com.stripe.example.javaapp.model.ConnectionToken;
import com.stripe.example.javaapp.model.PaymentIntentCreationResponse;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * The `BackendService` interface handles the two simple calls we need to make to our backend.
 */
public interface BackendService {

    /**
     * Get a connection token string from the backend
     */
    @POST("connection_token")
    Call<ConnectionToken> getConnectionToken();

    /**
     * Capture a specific payment intent on our backend
     */
    @FormUrlEncoded
    @POST("capture_payment_intent")
    Call<Void> capturePaymentIntent(@Field("payment_intent_id") @NotNull String id);

    /**
     * Create a PaymentIntent in example backend and return PaymentIntentCreationResponse
     * For internet readers, you need to create paymentIntent in backend
     * https://stripe.com/docs/terminal/payments/collect-payment?terminal-sdk-platform=android#create-payment
     */
    @FormUrlEncoded
    @POST("create_payment_intent")
    Call<PaymentIntentCreationResponse> createPaymentIntent(
            @FieldMap Map<String, String> createPaymentIntentParams
    );
}

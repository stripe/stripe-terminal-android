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
     * Capture a specific payment intent on our backend
     */
    @FormUrlEncoded
    @POST("cancel_payment_intent")
    Call<Void> cancelPaymentIntent(@Field("payment_intent_id") @NotNull String id);
}

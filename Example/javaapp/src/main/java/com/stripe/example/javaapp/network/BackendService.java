package com.stripe.example.javaapp.network;

import com.stripe.example.javaapp.model.ConnectionToken;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.http.Field;
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
     * Create a new reader location
     */
    @FormUrlEncoded
    @POST("create_location")
    Call<Void> createLocation(
            @Field("display_name") @NotNull String displayName,
            @Field("address[line1]") @NotNull String line1,
            @Field("address[line2]") String line2,
            @Field("address[city]") String city,
            @Field("address[postal_code]") String postalCode,
            @Field("address[state]") String state,
            @Field("address[country]") @NotNull String country
    );

    /**
     * Capture a specific payment intent on our backend
     */
    @FormUrlEncoded
    @POST("capture_payment_intent")
    Call<Void> capturePaymentIntent(@Field("payment_intent_id") @NotNull String id);

    /**
     * Cancel a specific payment intent on our backend
     */
    @FormUrlEncoded
    @POST("cancel_payment_intent")
    Call<Void> cancelPaymentIntent(@Field("payment_intent_id") @NotNull String id);
}

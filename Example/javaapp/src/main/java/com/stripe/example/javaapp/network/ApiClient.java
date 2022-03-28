package com.stripe.example.javaapp.network;

import com.stripe.example.javaapp.model.ConnectionToken;
import com.stripe.example.javaapp.model.PaymentIntentCreationResponse;
import com.stripe.stripeterminal.external.models.ConnectionTokenException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The `ApiClient` is a singleton object used to make calls to our backend and return their results
 */
public class ApiClient {

    /**
     * To get started with this demo, you'll need to first deploy an instance of
     * our provided example backend:
     * <p>
     * https://github.com/stripe/example-terminal-backend
     * <p>
     * After deploying your backend, replace "" on the line below with the URL of your Heroku app.
     * <p>
     * const val BACKEND_URL = "https://your-app.herokuapp.com"
     */
    public static final String BACKEND_URL = "";

    private static final Retrofit mRetrofit = new Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .client(new OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private static final BackendService mService = mRetrofit.create(BackendService.class);

    @NotNull
    public static String createConnectionToken() throws ConnectionTokenException {
        try {
            final Response<ConnectionToken> result = mService.getConnectionToken().execute();
            if (result.isSuccessful() && result.body() != null) {
                return result.body().getSecret();
            } else {
                throw new ConnectionTokenException("Creating connection token failed");
            }
        } catch (IOException e) {
            throw new ConnectionTokenException("Creating connection token failed", e);
        }
    }

    public static void createLocation(
            String displayName,
            String city,
            String country,
            String line1,
            String line2,
            String postalCode,
            String state
    ) {
        // TODO: Call backend to create location
    }

    public static void capturePaymentIntent(@NotNull String id) throws IOException {
        mService.capturePaymentIntent(id).execute();
    }

    /**
     * This method is calling the example backend (https://github.com/stripe/example-terminal-backend)
     * to create paymentIntent for Internet based readers, for example WisePOS E. For your own application, you
     * should create paymentIntent in your own merchant backend.
     */
    public static void createPaymentIntent(
            Long amount,
            String currency,
            boolean extendedAuth,
            boolean incrementalAuth,
            Callback<PaymentIntentCreationResponse> callback
    ) {
        final Map<String, String> createPaymentIntentParams = new HashMap<>();
        createPaymentIntentParams.put("amount", amount.toString());
        createPaymentIntentParams.put("currency", currency);
        if (extendedAuth) {
            createPaymentIntentParams.put("payment_method_options[card_present[request_extended_authorization]]", "true");
        }
        if (incrementalAuth) {
            createPaymentIntentParams.put("payment_method_options[card_present[request_incremental_authorization_support]]", "true");
        }

        mService.createPaymentIntent(createPaymentIntentParams).enqueue(callback);
    }
}

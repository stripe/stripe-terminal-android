package com.stripe.example.javaapp.network;

import com.stripe.example.javaapp.BuildConfig;
import com.stripe.example.javaapp.model.ConnectionToken;
import com.stripe.stripeterminal.external.models.ConnectionTokenException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The `ApiClient` is a singleton object used to make calls to our backend and return their results
 */
public class ApiClient {
    private static final Retrofit mRetrofit = new Retrofit.Builder()
            .baseUrl(BuildConfig.EXAMPLE_BACKEND_URL)
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
            @NotNull String displayName,
            @NotNull String line1,
            String line2,
            String city,
            String postalCode,
            String state,
            @NotNull String country
    ) throws Exception {
        try {
            final Response<Void> result = mService.createLocation(
                    displayName,
                    line1,
                    line2,
                    city,
                    postalCode,
                    state,
                    country
            ).execute();
            if (!result.isSuccessful()) {
                throw new Exception("Creating location failed");
            }
        } catch (IOException e) {
            throw new Exception("Creating location failed", e);
        }
    }

    public static void capturePaymentIntent(@NotNull String id) throws IOException {
        mService.capturePaymentIntent(id).execute();
    }

    public static void cancelPaymentIntent(
            String id,
            Callback<Void> callback
    ) {
        mService.cancelPaymentIntent(id).enqueue(callback);
    }
}

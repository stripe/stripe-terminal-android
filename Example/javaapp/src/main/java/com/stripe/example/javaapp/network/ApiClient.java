package com.stripe.example.javaapp.network;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.stripe.example.javaapp.model.ConnectionToken;
import com.stripe.stripeterminal.model.external.ConnectionTokenException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.OkHttpClient;
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
     *
     * https://github.com/stripe/example-terminal-backend
     *
     * After deploying your backend, replace "" on the line below with the URL of your Heroku app.
     *
     * const val BACKEND_URL = "https://your-app.herokuapp.com"
     */
    public static final String BACKEND_URL = "";

    private static final OkHttpClient mClient = new OkHttpClient.Builder()
            .addNetworkInterceptor(new StethoInterceptor())
            .build();
    private static final Retrofit mRetrofit = new Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .client(mClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private static final BackendService mService = mRetrofit.create(BackendService.class);

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

    public static void capturePaymentIntent(@NotNull String id) throws IOException {
        mService.capturePaymentIntent(id).execute();
    }
}

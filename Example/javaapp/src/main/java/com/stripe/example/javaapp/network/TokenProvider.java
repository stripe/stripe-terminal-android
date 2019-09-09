package com.stripe.example.javaapp.network;

import com.stripe.stripeterminal.callable.ConnectionTokenCallback;
import com.stripe.stripeterminal.callable.ConnectionTokenProvider;
import com.stripe.stripeterminal.model.external.ConnectionTokenException;

/**
 * A simple implementation of the [ConnectionTokenProvider] interface. We just request a
 * new token from our backend simulator and forward any exceptions along to the SDK.
 */
public class TokenProvider implements ConnectionTokenProvider {

    @Override
    public void fetchConnectionToken(ConnectionTokenCallback callback) {
        try {
            final String token = ApiClient.createConnectionToken();
            callback.onSuccess(token);
        } catch (ConnectionTokenException e) {
            callback.onFailure(e);
        }
    }
}

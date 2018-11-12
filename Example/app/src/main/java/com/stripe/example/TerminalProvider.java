package com.stripe.example;

import android.content.Context;

import com.stripe.exception.StripeException;
import com.stripe.stripeterminal.ConnectionTokenCallback;
import com.stripe.stripeterminal.ConnectionTokenException;
import com.stripe.stripeterminal.ConnectionTokenProvider;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.TerminalConfiguration;
import com.stripe.stripeterminal.TerminalConfiguration.LogLevel;

/**
 * The {@code TerminalProvider} will create a new {@link Terminal} instance the first time it's
 * called but will then just return the existing instance for all future calls.
 */
public class TerminalProvider {

    private static Terminal instance;

    /**
     * Create or return the {@link Terminal} instance
     * @param context The current context, in case it's necessary for Terminal creation
     * @return The Terminal instance
     */
    public static Terminal getInstance(Context context) {
        if (instance == null) {
            instance = Terminal.initTerminal(context, new TerminalConfiguration(LogLevel.VERBOSE),
                    new TokenProvider(), new TerminalEventListener());
        }

        return instance;
    }

    /**
     * A simple implementation of the {@link ConnectionTokenProvider} interface. We just request a
     * new token from our backend simulator and forward any exceptions along to the SDK.
     */
    private static class TokenProvider implements ConnectionTokenProvider {

        @Override
        public void fetchConnectionToken(ConnectionTokenCallback callback) {
            try {
                String token = BackendSimulator.createConnectionToken();
                callback.onSuccess(token);
            } catch (StripeException e) {
                callback.onFailure(new ConnectionTokenException(e.getMessage(), e));
            }
        }
    }
}

package com.stripe.example;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.terminal.ConnectionToken;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code BackendSimulator} has been included as part of the example app's client-side code for
 * demonstration purposes only. Secret API keys should be kept confidential and stored only on your
 * own servers. Your account's secret API key can perform any API request to Stripe without
 * restriction. You should NEVER hardcode your secret API key into your app, and only call these
 * API endpoints from your backend.
 *
 * @see <a href="https://stripe.com/docs/keys">Keys</a>
 */
public class BackendSimulator {

    private static final String API_KEY = "sk_test_032NCVVTLNB9UJ63lQcD29ZK";

    /**
     * Create a connection token with the Stripe Java bindings
     * @return The string form of the token
     * @throws StripeException
     */
    static String createConnectionToken() throws StripeException {
        Stripe.apiKey = API_KEY;
        Map<String, Object> params = new HashMap<>();
        return ConnectionToken.create(params).getSecret();
    }
}

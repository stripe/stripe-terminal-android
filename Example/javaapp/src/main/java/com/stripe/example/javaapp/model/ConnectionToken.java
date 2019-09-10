package com.stripe.example.javaapp.model;

import org.jetbrains.annotations.NotNull;

/**
 * A one-field data class used to handle the connection token response from our backend
 */
public class ConnectionToken {
    @NotNull private final String secret;

    public ConnectionToken(@NotNull String secret) {
        this.secret = secret;
    }

    @NotNull
    public String getSecret() {
        return secret;
    }
}

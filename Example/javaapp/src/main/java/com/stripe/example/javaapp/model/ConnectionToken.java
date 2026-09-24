package com.stripe.example.javaapp.model;

import androidx.annotation.Keep;

import org.jetbrains.annotations.NotNull;

/**
 * A one-field data class used to handle the connection token response from our backend
 */
@Keep // Gson populates this response through reflection, including in minified builds.
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

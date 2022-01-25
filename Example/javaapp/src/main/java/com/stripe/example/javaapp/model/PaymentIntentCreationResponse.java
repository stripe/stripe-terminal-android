package com.stripe.example.javaapp.model;

public class PaymentIntentCreationResponse {
    private String secret;
    private String intent;

    public PaymentIntentCreationResponse(String secret, String intent) {
        this.secret = secret;
        this.intent = intent;
    }

    public String getSecret() {
        return secret;
    }

    public String getIntent() {
        return intent;
    }
}

package com.stripe.example.javaapp.model;

import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stripe.stripeterminal.external.models.PaymentIntent;

public class OfflineLog implements Comparable<OfflineLog> {

    final long timestamp;

    @NonNull final String message;
    @Nullable final PaymentIntent details;
    public OfflineLog(long timestamp,  @NonNull String message, @Nullable PaymentIntent details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    public String toMessage() {
        return DateFormat.format("yyyy-MM-dd HH:mm:ss", timestamp).toString() + " : " + message;
    }
    @Override
    public int compareTo(OfflineLog o) {
        return (int) (this.timestamp - o.timestamp);
    }
}

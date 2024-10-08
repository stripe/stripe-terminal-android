package com.stripe.example.javaapp;

import android.util.Log;

import com.stripe.stripeterminal.external.callable.TerminalListener;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.PaymentStatus;

import org.jetbrains.annotations.NotNull;

/**
 * The `TerminalEventListener` implements the [TerminalListener] interface and will
 * forward along any events to other parts of the app that register for updates.
 */
public class TerminalEventListener extends ListenerAnnouncer<TerminalListener> implements TerminalListener {
    private static final String TAG = TerminalEventListener.class.toString();

    public static final TerminalEventListener instance = new TerminalEventListener();

    private TerminalEventListener() {

    }

    @Override
    public void onConnectionStatusChange(@NotNull ConnectionStatus status) {
        Log.i("ConnectionStatusChange", status.toString());
        this.announce(listener -> listener.onConnectionStatusChange(status));
    }

    @Override
    public void onPaymentStatusChange(@NotNull PaymentStatus status) {
        Log.i("PaymentStatusChange", status.toString());
        this.announce(listener -> listener.onPaymentStatusChange(status));
    }
}

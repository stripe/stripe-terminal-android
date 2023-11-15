package com.stripe.example.javaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;

import com.stripe.stripeterminal.external.OfflineMode;
import com.stripe.stripeterminal.external.callable.OfflineListener;
import com.stripe.stripeterminal.external.models.OfflineStatus;
import com.stripe.stripeterminal.external.models.PaymentIntent;
import com.stripe.stripeterminal.external.models.TerminalException;

@OptIn(markerClass = OfflineMode.class)
public class TerminalOfflineListener extends ListenerAnnouncer<OfflineListener> implements OfflineListener {
    public static final TerminalOfflineListener instance = new TerminalOfflineListener();

    private TerminalOfflineListener() {

    }

    @Override
    public void onOfflineStatusChange(@NonNull OfflineStatus offlineStatus) {
        this.announce(offlineListener -> offlineListener.onOfflineStatusChange(offlineStatus));
    }

    @Override
    public void onPaymentIntentForwarded(@NonNull PaymentIntent paymentIntent, @Nullable TerminalException e) {
        this.announce(offlineListener -> offlineListener.onPaymentIntentForwarded(paymentIntent, e));
    }

    @Override
    public void onForwardingFailure(@NonNull TerminalException e) {
        this.announce(offlineListener -> offlineListener.onForwardingFailure(e));
    }
}

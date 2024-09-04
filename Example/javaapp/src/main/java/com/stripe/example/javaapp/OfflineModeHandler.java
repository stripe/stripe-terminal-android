package com.stripe.example.javaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.lifecycle.MutableLiveData;

import com.stripe.example.javaapp.model.OfflineLog;
import com.stripe.example.javaapp.network.ApiClient;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.OfflineMode;
import com.stripe.stripeterminal.external.callable.OfflineListener;
import com.stripe.stripeterminal.external.models.OfflineStatus;
import com.stripe.stripeterminal.external.models.PaymentIntent;
import com.stripe.stripeterminal.external.models.PaymentIntentStatus;
import com.stripe.stripeterminal.external.models.TerminalException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@OptIn(markerClass = OfflineMode.class)
public class OfflineModeHandler implements OfflineListener {
    interface Callback {
        void makeToast(String message);
    }

    private final Callback callback;

    private int successfulForwardCount = 0;
    private int failedForwardCount = 0;

    public List<OfflineLog> logs = new ArrayList<>();

    public MutableLiveData<List<OfflineLog>> liveLogs = new MutableLiveData<>(new ArrayList<>());

    public OfflineModeHandler(Callback callback) {
        this.callback = callback;
    }

    public void createLog(String logString, @Nullable PaymentIntent details) {
        logs.add(new OfflineLog(System.currentTimeMillis(), logString, details));
        Collections.sort(logs);
        liveLogs.postValue(logs);
    }

    @Override
    public void onOfflineStatusChange(@NonNull OfflineStatus offlineStatus) {
        switch (offlineStatus.getSdk().getNetworkStatus()) {
            case UNKNOWN:
                createLog("Transitioned state to unknown.", null);
            case OFFLINE:
                createLog("Transitioned state to offline.", null);
            case ONLINE:
                createLog("Transitioned state to online.", null);
        }
    }

    @Override
    public void onPaymentIntentForwarded(@NonNull PaymentIntent paymentIntent, @Nullable TerminalException e) {
        if (e != null) {
            failedForwardCount++;
            String id = paymentIntent.getOfflineDetails() != null ? paymentIntent.getOfflineDetails().getId() : paymentIntent.getDescription();
            callback.makeToast(String.format(Locale.ROOT, "⚠️ Error forwarding payment: %s\n%s", id, e.getErrorMessage()));

            createLog(String.format("Error forwarding offline payment intent: %s\n%s", id, e.getErrorMessage()), paymentIntent);
        } else {
            successfulForwardCount++;
            String offlineId = paymentIntent.getOfflineDetails() != null ? paymentIntent.getOfflineDetails().getId() : "null offline id";
            String status = paymentIntent.getStatus() != null ? paymentIntent.getStatus().toString() : "null status";
            createLog(String.format("Successfully forwarded offline payment intent: %s with offline id %s status %s",
                    paymentIntent.getId(), offlineId, status), paymentIntent);

            if (paymentIntent.getStatus() == PaymentIntentStatus.REQUIRES_CAPTURE && paymentIntent.getId() != null) {
                try {
                    ApiClient.capturePaymentIntent(paymentIntent.getId());
                    String message = String.format(
                            Locale.ROOT,
                            "Successfully captured offline payment intent for %s: %d%s",
                            paymentIntent.getId(),
                            paymentIntent.getAmount(),
                            paymentIntent.getCurrency()
                    );
                    createLog(message, paymentIntent);
                } catch (IOException ioException) {
                    String message = String.format("Error capturing offline payment intent for %s:%s", paymentIntent.getId(), ioException.getMessage());
                    createLog(message, paymentIntent);
                }
            }
        }

        if (Terminal.getInstance().getOfflineStatus().getSdk().getOfflinePaymentsCount() == 0) {
            reportForwardCountsAndReset();
        }
    }

    @Override
    public void onForwardingFailure(@NonNull TerminalException e) {
        callback.makeToast("⚠️ Error forwarding: " + e.getErrorMessage());
        createLog("Did report forwarding error: " + e.getErrorMessage(), null);
    }

    private void reportForwardCountsAndReset() {
        if (successfulForwardCount == 0 || failedForwardCount == 0) {
            return;
        }
        callback.makeToast(String.format(Locale.getDefault(), "✅ Forwarded %d payment(s)\n⚠️ Failed to forward %d payment(s)", successfulForwardCount, failedForwardCount));
        successfulForwardCount = 0;
        failedForwardCount = 0;
    }
}

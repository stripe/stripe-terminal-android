package com.stripe.example;

import com.google.common.collect.Lists;
import com.stripe.stripeterminal.ConnectionStatus;
import com.stripe.stripeterminal.PaymentStatus;
import com.stripe.stripeterminal.Reader;
import com.stripe.stripeterminal.ReaderEvent;
import com.stripe.stripeterminal.TerminalListener;

import java.util.List;

/**
 * The {@code TerminalEventListener} implements the {@link TerminalListener} interface and will
 * forward along any events to other parts of the app that register for updates.
 */
public class TerminalEventListener implements TerminalListener {

    private static List<TerminalListener> listeners = Lists.newArrayList();

    private static ConnectionStatus currentConnectionStatus = ConnectionStatus.NOT_CONNECTED;
    private static PaymentStatus currentPaymentStatus = PaymentStatus.NOT_READY;

    static void registerListener(TerminalListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onReportReaderEvent(ReaderEvent event) {
        for (TerminalListener listener : listeners) {
            listener.onReportReaderEvent(event);
        }
    }

    @Override
    public void onReportLowBatteryWarning() {
        for (TerminalListener listener : listeners) {
            listener.onReportLowBatteryWarning();
        }
    }

    @Override
    public void onUnexpectedReaderDisconnect(Reader reader) {
        for (TerminalListener listener : listeners) {
            listener.onUnexpectedReaderDisconnect(reader);
        }
    }

    @Override
    public void onConnectionStatusChange(ConnectionStatus status) {
        currentConnectionStatus = status;
        for (TerminalListener listener : listeners) {
            listener.onConnectionStatusChange(status);
        }
    }

    @Override
    public void onPaymentStatusChange(PaymentStatus status) {
        currentPaymentStatus = status;
        for (TerminalListener listener : listeners) {
            listener.onPaymentStatusChange(status);
        }
    }

    /**
     * @return the current ConnectionStatus
     */
    public static ConnectionStatus getCurrentConnectionStatus() {
        return currentConnectionStatus;
    }

    /**
     * @return the current PaymentStatus
     */
    public static PaymentStatus getCurrentPaymentStatus() {
        return currentPaymentStatus;
    }
}

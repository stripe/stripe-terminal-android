package com.stripe.example

import android.util.Log
import com.stripe.stripeterminal.callable.TerminalListener
import com.stripe.stripeterminal.model.external.ConnectionStatus
import com.stripe.stripeterminal.model.external.PaymentStatus
import com.stripe.stripeterminal.model.external.Reader
import com.stripe.stripeterminal.model.external.ReaderEvent

/**
 * The `TerminalEventListener` implements the [TerminalListener] interface and will
 * forward along any events to other parts of the app that register for updates.
 *
 * TODO: Finish implementing
 */
class TerminalEventListener : TerminalListener {

    override fun onReportReaderEvent(event: ReaderEvent) {
        Log.i("ReaderEvent", event.toString())
    }

    override fun onReportLowBatteryWarning() {
        Log.i("LowBatteryWarning", "")
    }

    override fun onUnexpectedReaderDisconnect(reader: Reader) {
        Log.i("UnexpectedDisconnect", reader.serialNumber ?: "reader's serialNumber is null!")
    }

    override fun onConnectionStatusChange(status: ConnectionStatus) {
        Log.i("ConnectionStatusChange", status.toString())
    }

    override fun onPaymentStatusChange(status: PaymentStatus) {
        Log.i("PaymentStatusChange", status.toString())
    }
}

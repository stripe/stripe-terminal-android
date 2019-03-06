package com.stripe.example

import android.util.Log
import com.stripe.stripeterminal.*

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
        Log.i("UnexpectedDisconnect", reader.serialNumber)
    }

    override fun onConnectionStatusChange(status: ConnectionStatus) {
        Log.i("ConnectionStatusChange", status.toString())
    }

    override fun onPaymentStatusChange(status: PaymentStatus) {
        Log.i("PaymentStatusChange", status.toString())
    }
}

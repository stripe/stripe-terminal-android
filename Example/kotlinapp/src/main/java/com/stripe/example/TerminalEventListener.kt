package com.stripe.example

import android.util.Log
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.ConnectionStatus
import com.stripe.stripeterminal.external.models.PaymentStatus

/**
 * The `TerminalEventListener` implements the [TerminalListener] interface and will
 * forward along any events to other parts of the app that register for updates.
 */
object TerminalEventListener : TerminalListener {

    override fun onConnectionStatusChange(status: ConnectionStatus) {
        Log.i("ConnectionStatusChange", status.toString())
    }

    override fun onPaymentStatusChange(status: PaymentStatus) {
        Log.i("PaymentStatusChange", status.toString())
    }
}

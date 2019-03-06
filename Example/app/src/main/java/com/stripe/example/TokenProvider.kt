package com.stripe.example

import com.stripe.exception.StripeException
import com.stripe.stripeterminal.ConnectionTokenCallback
import com.stripe.stripeterminal.ConnectionTokenException
import com.stripe.stripeterminal.ConnectionTokenProvider

/**
 * A simple implementation of the [ConnectionTokenProvider] interface. We just request a
 * new token from our backend simulator and forward any exceptions along to the SDK.
 */
class TokenProvider : ConnectionTokenProvider {

    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
        try {
            val token = BackendSimulator.createConnectionToken()
            callback.onSuccess(token)
        } catch (e: StripeException) {
            callback.onFailure(ConnectionTokenException(e.message, e))
        }
    }
}
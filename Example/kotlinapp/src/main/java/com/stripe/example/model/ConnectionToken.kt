package com.stripe.example.model

import androidx.annotation.Keep

/**
 * A one-field data class used to handle the connection token response from our backend
 */
@Keep // Gson populates this response through reflection, including in minified builds.
data class ConnectionToken(val secret: String)

package com.stripe.example.model

enum class ConnectionType(val type: String) {
    SMART_READER("reader"), SDK("sdk");

    companion object {
        fun fromType(type: String): ConnectionType {
            return when (type) {
                SMART_READER.type -> SMART_READER
                SDK.type -> SDK
                else -> throw IllegalArgumentException("Unknown connection type: $type")
            }
        }
    }
}

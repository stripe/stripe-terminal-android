package com.stripe.example.fragment.discovery

/**
 * Discovery options for how to discover readers of different types.
 */
enum class DiscoveryMethod {
    BLUETOOTH_SCAN,
    INTERNET,
    TAP_TO_PAY,
    USB,
}

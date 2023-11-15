package com.stripe.example.model

data class OfflineForwardingSummary(
    val successful: Int,
    val failed: Int
) {
    @Transient
    val hasNoEvents: Boolean = (successful + failed) == 0
}

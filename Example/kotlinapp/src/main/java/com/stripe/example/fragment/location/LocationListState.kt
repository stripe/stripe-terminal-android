package com.stripe.example.fragment.location

import com.stripe.stripeterminal.external.models.Location

/**
 * Represents the current view state of the location list.
 */
data class LocationListState(
    val locations: List<Location> = emptyList(),
    val isLoading: Boolean = true,
    val hasMore: Boolean = true,
) {
    /**
     * Whether the header view should be displayed in the list.
     */
    val headerVisible = locations.isNotEmpty() || !isLoading
}

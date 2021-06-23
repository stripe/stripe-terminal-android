package com.stripe.example.fragment.location

import com.stripe.stripeterminal.external.models.Location

/**
 * Controls the selected reader location to use for connections.
 */
interface LocationSelectionController {
    /**
     * Invoked when the user has selected a new location to use for connection.
     */
    fun onLocationSelected(location: Location)

    /**
     * Invoked when the user has cleared the currently selected location.
     *
     * This should revert functionality back to "last used"
     */
    fun onLocationCleared()
}

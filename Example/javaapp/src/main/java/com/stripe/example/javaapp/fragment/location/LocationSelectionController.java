package com.stripe.example.javaapp.fragment.location;

import com.stripe.stripeterminal.external.models.Location;

/**
 * Controls the selected reader location to use for connections.
 */
public interface LocationSelectionController {
    /**
     * Invoked when the user has selected a new location to use for connection.
     */
    void onLocationSelected(Location location);

    /**
     * Invoked when the user has cleared the currently selected location.
     *
     * This should revert functionality back to "last used"
     */
    void onLocationCleared();
}

package com.stripe.example.javaapp.fragment.location;

import com.stripe.stripeterminal.external.models.Location;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Represents the current view state of the location list.
 */
final public class LocationListState {
    private List<Location> locations;
    private boolean isLoading;
    private boolean hasMore;

    public LocationListState(List<Location> locations, boolean isLoading, boolean hasMore) {
        this.locations = locations;
        this.isLoading = isLoading;
        this.hasMore = hasMore;
    }

    public LocationListState() {
        this(Collections.emptyList(), true, true);
    }

    public boolean hasMore() {
        return hasMore;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public List<Location> getLocations() {
        return locations;
    }

    /**
     * @return Whether the header view should be displayed in the list.
     */
    public boolean headerVisible() {
        return !locations.isEmpty() || !isLoading();
    }
}

package com.stripe.example.javaapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.stripe.example.javaapp.fragment.location.LocationListState;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.LocationListCallback;
import com.stripe.stripeterminal.external.models.ListLocationsParameters;
import com.stripe.stripeterminal.external.models.Location;
import com.stripe.stripeterminal.external.models.TerminalException;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final public class LocationSelectionViewModel extends ViewModel {
    private static final int LOCATION_QUERY_LIMIT = 100;

    final private MutableLiveData<LocationListState> mutableListState = new MutableLiveData<>(new LocationListState());
    public LiveData<LocationListState> listState = mutableListState;

    final private MutableLiveData<TerminalException> mutableError = new MutableLiveData<>();
    public LiveData<TerminalException> error = mutableError;

    final private LocationListCallback locationCallback = new LocationListCallback() {
        @Override
        public void onSuccess(@NotNull List<Location> locations, boolean hasMore) {
            LocationListState current = mutableListState.getValue();
            List<Location> combinedLocations = new ArrayList<>();
            combinedLocations.addAll(current.getLocations());
            combinedLocations.addAll(locations);
            LocationListState newState = new LocationListState(
                    combinedLocations,
                    false,
                    hasMore
            );
            mutableListState.postValue(newState);
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            mutableError.postValue(e);
        }
    };

    public LocationSelectionViewModel() {
        super();
        load();
    }

    public void loadMoreLocations() {
        if (listState.getValue().isLoading()) {
            return;
        }
        if (!listState.getValue().hasMore()) {
            return;
        }
        LocationListState current = mutableListState.getValue();
        LocationListState newState = new LocationListState(
            current.getLocations(),
            true,
            current.hasMore()
        );

        mutableListState.postValue(newState);

        ListLocationsParameters.Builder builder = new ListLocationsParameters.Builder();
        builder.setLimit(LOCATION_QUERY_LIMIT);
        builder.setStartingAfter(current.getLocations().get(current.getLocations().size() - 1).getId());
        Terminal.getInstance().listLocations(builder.build(), locationCallback);
    }

    public void reload() {
        mutableListState.postValue(new LocationListState());
        load();
    }

    private void load() {
        ListLocationsParameters.Builder builder = new ListLocationsParameters.Builder();
        builder.setLimit(LOCATION_QUERY_LIMIT);
        Terminal.getInstance().listLocations(builder.build(), locationCallback);
    }
}

package com.stripe.example.javaapp.fragment.location;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.R;
import com.stripe.stripeterminal.external.models.Location;

/**
 * Adapts a list of locations for a RecyclerView with a header and loading view.
 */
public class LocationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final LayoutInflater inflater;
    private final LocationSelectionController locationSelectionController;
    private LocationListState locationListState = null;

    public LocationListAdapter(
        LayoutInflater inflater,
        LocationSelectionController locationSelectionController
    ) {
        super();
        this.inflater = inflater;
        this.locationSelectionController = locationSelectionController;
    }

    public void setLocationListState(LocationListState state) {
        locationListState = state;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == R.layout.list_item_card) {
            return new LocationListViewHolder(
                    inflater.inflate(viewType, parent, false),
                    locationSelectionController
            );
        } else if (viewType == R.layout.list_item_progress) {
            return new ProgressViewHolder(
                    inflater.inflate(viewType, parent, false)
            );
        } else if (viewType == R.layout.list_item_header) {
            return new HeaderViewHolder(
                    inflater.inflate(viewType, parent, false)
            );
        } else {
            throw new IllegalStateException("Unknown View Type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LocationListViewHolder) {
            Location location = locationListState.getLocations().get(position - 1);
            ((LocationListViewHolder) holder).bind(location);
        } else if (holder instanceof HeaderViewHolder) {
            int size = locationListState == null ? 0 : locationListState.getLocations().size();
            boolean hasMore = locationListState != null && locationListState.hasMore();
            ((HeaderViewHolder) holder).bind(size, hasMore);
        }
    }

    @Override
    public int getItemCount() {
        if (locationListState == null) return 0;

        int header = locationListState.headerVisible() ? 1 : 0;
        int loading = locationListState.isLoading() ? 1 : 0;
        int locations = locationListState.getLocations().size();

        return header + locations + loading;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && locationListState.headerVisible()) {
            return R.layout.list_item_header;
        } else if (position == getItemCount() - 1 && locationListState.isLoading()) {
            return R.layout.list_item_progress;
        } else {
            return R.layout.list_item_card;
        }
    }
}

package com.stripe.example.javaapp.fragment.location;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.stripe.stripeterminal.external.models.Location;
import com.stripe.example.javaapp.databinding.ListItemCardBinding;

/**
 * Bind a Location to a list item card.
 */
final public class LocationListViewHolder extends RecyclerView.ViewHolder {
    private LocationSelectionController locationSelectionController;
    private ListItemCardBinding binding;

    public LocationListViewHolder(View parent, LocationSelectionController locationSelectionController) {
        super(parent);
        this.locationSelectionController = locationSelectionController;
        this.binding = ListItemCardBinding.bind(parent);
    }

    public void bind(Location location) {
        binding.listItemCardTitle.setText(location.getDisplayName());
        binding.listItemCardDescription.setText(location.getId());
        binding.listItemCard.setOnClickListener((View.OnClickListener) v -> {
            locationSelectionController.onLocationSelected(location);
        });
    }
}

package com.stripe.example.javaapp.fragment.discovery;

import android.content.res.Resources;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.R;
import com.stripe.stripeterminal.external.models.Location;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.example.javaapp.databinding.ListItemCardBinding;

import org.jetbrains.annotations.NotNull;

/**
 * A simple [RecyclerView.ViewHolder] that also acts as a [View.OnClickListener] to allow for
 * selecting a reader.
 */
public class ReaderHolder extends RecyclerView.ViewHolder {
    @NotNull private final ReaderClickListener clickListener;
    @NotNull private final ListItemCardBinding binding;
    @NotNull private final Resources resources;

    public ReaderHolder(
        @NotNull View parent,
        @NotNull ReaderClickListener clickListener
    ) {
        super(parent);
        this.binding = ListItemCardBinding.bind(parent);
        this.resources = parent.getResources();
        this.clickListener = clickListener;
    }

    void bind(@NotNull Reader reader, Location location) {
        binding.listItemCardTitle.setText(
            reader.getSerialNumber() != null ? reader.getSerialNumber() : reader.getId()
        );
        if (location == null && reader.getLocation() == null) {
            binding.listItemCardDescription.setText(R.string.discovery_reader_location_unavailable);
        } else if (location == null) {
            binding.listItemCardDescription.setText(
                resources.getString(
                    R.string.discovery_reader_location_last,
                    reader.getLocation().getDisplayName()
                )
            );
        } else {
            binding.listItemCardDescription.setText(
                resources.getString(R.string.discovery_reader_location, location.getDisplayName())
            );
        }
        binding.listItemCard.setOnClickListener((View.OnClickListener) v -> clickListener.onClick(reader));
    }
}

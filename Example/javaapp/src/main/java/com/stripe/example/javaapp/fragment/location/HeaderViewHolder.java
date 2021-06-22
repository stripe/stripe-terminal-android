package com.stripe.example.javaapp.fragment.location;

import android.content.res.Resources;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.databinding.ListItemHeaderBinding;

/**
 * Bind Info about a location list to a header view.
 */
public class HeaderViewHolder extends RecyclerView.ViewHolder {
    private Resources resources;
    private ListItemHeaderBinding binding;

    public HeaderViewHolder(View parent) {
        super(parent);
        resources = parent.getResources();
        binding = ListItemHeaderBinding.bind(parent);
    }

    public void bind(int size, boolean hasMore) {
        int titleId = hasMore ? R.string.select_location_list_title_more : R.string.select_location_list_title;
        binding.listItemHeader.setText(resources.getString(titleId, size));
    }
}

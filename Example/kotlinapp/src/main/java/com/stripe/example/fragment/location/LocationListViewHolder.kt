package com.stripe.example.fragment.location

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.databinding.ListItemCardBinding
import com.stripe.stripeterminal.external.models.Location

/**
 * Bind a Location to a list item card.
 */
class LocationListViewHolder(
    parent: View,
    private val locationSelectionController: LocationSelectionController,
) : RecyclerView.ViewHolder(parent) {
    val binding = ListItemCardBinding.bind(parent)

    fun bind(location: Location) {
        binding.listItemCardTitle.text = location.displayName
        binding.listItemCardDescription.text = location.id
        binding.listItemCard.setOnClickListener {
            locationSelectionController.onLocationSelected(location)
        }
    }
}

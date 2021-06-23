package com.stripe.example.fragment.discovery

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.R
import com.stripe.example.databinding.ListItemCardBinding
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader

/**
 * A simple [RecyclerView.ViewHolder] that also acts as a [View.OnClickListener] to allow for
 * selecting a reader.
 */
class ReaderHolder(
    parent: View,
    private val clickListener: ReaderClickListener,
) : RecyclerView.ViewHolder(parent) {
    private val binding = ListItemCardBinding.bind(parent)
    private val resources = parent.resources

    fun bind(reader: Reader, locationSelection: Location?) {
        binding.listItemCardTitle.text = reader.serialNumber
            ?: reader.id
            ?: resources.getString(R.string.discovery_reader_unknown)
        binding.listItemCardDescription.text = when {
            locationSelection == null && reader.location == null -> resources.getString(
                R.string.discovery_reader_location_unavailable
            )
            locationSelection == null -> resources.getString(
                R.string.discovery_reader_location_last,
                reader.location!!.displayName
            )
            else -> resources.getString(
                R.string.discovery_reader_location,
                locationSelection.displayName
            )
        }
        binding.listItemCard.setOnClickListener {
            clickListener.onClick(reader)
        }
    }
}

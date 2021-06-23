package com.stripe.example.fragment.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.R
import java.lang.IllegalStateException

/**
 * Adapts a list of locations for a RecyclerView with a header and loading view.
 */
class LocationListAdapter(
    private val inflater: LayoutInflater,
    private val locationSelectionController: LocationSelectionController,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var locationListState: LocationListState? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.list_item_card -> LocationListViewHolder(
                parent = inflater.inflate(viewType, parent, false),
                locationSelectionController = locationSelectionController
            )
            R.layout.list_item_progress -> ProgressViewHolder(
                parent = inflater.inflate(viewType, parent, false)
            )
            R.layout.list_item_header -> HeaderViewHolder(
                parent = inflater.inflate(viewType, parent, false)
            )
            else -> throw IllegalStateException("Unknown View Type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LocationListViewHolder ->
                locationListState?.locations
                    ?.get(position - 1)
                    ?.also { holder.bind(it) }
            is HeaderViewHolder -> holder.bind(locationListState?.locations?.size ?: 0, locationListState?.hasMore ?: false)
        }
    }

    override fun getItemCount(): Int {
        val header = if (locationListState?.headerVisible == true) 1 else 0
        val loading = if (locationListState?.isLoading == true) 1 else 0
        val locations = locationListState?.locations?.size ?: 0

        return header + locations + loading
    }

    override fun getItemViewType(position: Int): Int = when {
        position == 0 && locationListState?.headerVisible == true -> R.layout.list_item_header
        position == itemCount - 1 && locationListState?.isLoading == true -> R.layout.list_item_progress
        else -> R.layout.list_item_card
    }
}

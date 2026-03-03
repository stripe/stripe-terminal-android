package com.stripe.example.fragment.event

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.databinding.ListItemEventBinding
import com.stripe.example.model.Event

/**
 * Our [RecyclerView.Adapter] implementation that allows us to update the list of events
 */
class EventAdapter : RecyclerView.Adapter<EventHolder>() {
    private val differ = AsyncListDiffer(this, ItemCallback())

    fun updateEvents(events: List<Event>) {
        differ.submitList(events)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        return EventHolder(
            ListItemEventBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    private class ItemCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}

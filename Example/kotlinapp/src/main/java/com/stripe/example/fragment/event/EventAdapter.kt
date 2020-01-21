package com.stripe.example.fragment.event

import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.model.Event
import com.stripe.example.viewmodel.EventViewModel

object EventsBindingAdapter {
    @BindingAdapter("events")
    @JvmStatic
    fun RecyclerView.bindItems(events: List<Event>) {
        val adapter = adapter as EventAdapter
        adapter.updateEvents(events)
    }
}

/**
 * Our [RecyclerView.Adapter] implementation that allows us to update the list of events
 */
class EventAdapter(
    viewModel: EventViewModel
) : RecyclerView.Adapter<EventHolder>() {
    private var events: List<Event> = viewModel.events.value ?: emptyList()

    fun updateEvents(events: List<Event>) {
        this.events = events
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        return EventHolder(parent)
    }
}

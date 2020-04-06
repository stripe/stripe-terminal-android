package com.stripe.example.javaapp.fragment.event;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.model.Event;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Our [RecyclerView.Adapter] implementation that allows us to update the list of events
 */
public class EventAdapter extends RecyclerView.Adapter<EventHolder> {
    @NotNull private List<Event> events;

    public EventAdapter() {
        super();
        events = new ArrayList<>();
    }

    void updateEvents(@NotNull List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    @Override
    public void onBindViewHolder(@NotNull EventHolder holder, int position) {
        holder.bind(events.get(position));
    }

    @NotNull
    @Override
    public EventHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new EventHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_event, parent, false));
    }
}

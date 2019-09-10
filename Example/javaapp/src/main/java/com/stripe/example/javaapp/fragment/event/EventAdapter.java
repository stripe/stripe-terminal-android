package com.stripe.example.javaapp.fragment.event;

import android.view.ViewGroup;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.stripe.example.javaapp.model.Event;
import com.stripe.example.javaapp.viewmodel.EventViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Our [RecyclerView.Adapter] implementation that allows us to update the list of events
 */
public class EventAdapter extends RecyclerView.Adapter<EventHolder> {
    @NotNull private final EventViewModel viewModel;
    @NotNull private List<Event> events;

    @BindingAdapter("events")
    public static void bindEvents(@NotNull RecyclerView recyclerView, @NotNull List<Event> events) {
        if (recyclerView.getAdapter() instanceof EventAdapter) {
            ((EventAdapter) recyclerView.getAdapter()).updateEvents(events);
        }
    }

    public EventAdapter(@NotNull EventViewModel viewModel) {
        super();
        this.viewModel = viewModel;
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
        return new EventHolder(parent);
    }
}

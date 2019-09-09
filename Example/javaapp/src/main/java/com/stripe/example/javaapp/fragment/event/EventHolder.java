package com.stripe.example.javaapp.fragment.event;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.library.baseAdapters.BR;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.databinding.ListItemEventBinding;
import com.stripe.example.javaapp.model.Event;

import org.jetbrains.annotations.NotNull;

/**
 * A simple [RecyclerView.ViewHolder] that displays various events
 */
public class EventHolder extends RecyclerView.ViewHolder {
    @NotNull private final ListItemEventBinding binding;

    public EventHolder(
        @NotNull ViewGroup parent
    ) {
        this(DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.list_item_event,
                parent,
                false
        ));
    }

    private EventHolder(
        @NotNull ListItemEventBinding binding
    ) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(@NotNull Event event) {
        binding.setVariable(BR.event, event);
        binding.executePendingBindings();
    }
}

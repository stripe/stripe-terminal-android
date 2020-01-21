package com.stripe.example.javaapp.fragment.event;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    public EventHolder(
        @NotNull View itemView
    ) {
        super(itemView);
    }

    public void bind(@NotNull Event event) {
        ((TextView) itemView.findViewById(R.id.method)).setText(event.getMethod());
        ((TextView) itemView.findViewById(R.id.message)).setText(event.getMessage());
    }
}

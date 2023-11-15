package com.stripe.example.javaapp.fragment.offline;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.model.Event;
import com.stripe.example.javaapp.model.OfflineLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Our [RecyclerView.Adapter] implementation that allows us to update the list of events
 */
public class OfflinePaymentsLogAdapter extends RecyclerView.Adapter<OfflinePaymentsLogHolder> {
    @NotNull private List<OfflineLog> logs = new ArrayList<>();

    void updateOfflineLogs(@NotNull List<OfflineLog> logs) {
        this.logs = logs;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    @Override
    public void onBindViewHolder(@NotNull OfflinePaymentsLogHolder holder, int position) {
        holder.bind(logs.get(position));
    }

    @NotNull
    @Override
    public OfflinePaymentsLogHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new OfflinePaymentsLogHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_offline_log, parent, false));
    }
}

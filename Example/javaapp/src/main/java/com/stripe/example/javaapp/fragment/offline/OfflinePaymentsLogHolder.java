package com.stripe.example.javaapp.fragment.offline;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.model.Event;
import com.stripe.example.javaapp.model.OfflineLog;

import org.jetbrains.annotations.NotNull;

/**
 * A simple [RecyclerView.ViewHolder] that displays various offline payments logs
 */
public class OfflinePaymentsLogHolder extends RecyclerView.ViewHolder {

    public OfflinePaymentsLogHolder(@NotNull View itemView) {
        super(itemView);
    }

    public void bind(@NotNull OfflineLog log) {
        ((TextView) itemView.findViewById(R.id.message)).setText(log.toMessage());
    }
}

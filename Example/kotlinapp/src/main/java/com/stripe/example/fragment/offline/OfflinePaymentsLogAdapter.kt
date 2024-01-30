package com.stripe.example.fragment.offline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.R
import com.stripe.example.model.OfflineEvent

/**
 * Our [RecyclerView.Adapter] implementation that allows us to update the list of events
 */
class OfflinePaymentsLogAdapter : RecyclerView.Adapter<OfflinePaymentsLogHolder>() {
    private var logs: List<OfflineEvent> = emptyList()

    fun updateOfflineLogs(logs: List<OfflineEvent>) {
        this.logs = logs
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return logs.size
    }

    override fun onBindViewHolder(holder: OfflinePaymentsLogHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflinePaymentsLogHolder {
        return OfflinePaymentsLogHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.list_item_offline_log, parent, false)
        )
    }
}

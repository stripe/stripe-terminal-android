package com.stripe.example.fragment.offline

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.R
import com.stripe.example.model.OfflineEvent

/**
 * A simple [RecyclerView.ViewHolder] that displays various offline payments logs
 */
class OfflinePaymentsLogHolder(
        itemView: View
) : RecyclerView.ViewHolder(itemView) {
    fun bind(event: OfflineEvent) {
        (itemView.findViewById<View>(R.id.message) as TextView).text = event.summary
    }
}

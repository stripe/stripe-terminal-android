package com.stripe.example.fragment.event

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.R
import com.stripe.example.databinding.ListItemEventBinding
import com.stripe.example.model.Event

/**
 * A simple [RecyclerView.ViewHolder] that displays various events
 */
class EventHolder(
    parent: ViewGroup,
    private val binding: ListItemEventBinding = DataBindingUtil.inflate(
        LayoutInflater.from(parent.context), R.layout.list_item_event, parent, false)
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {
        binding.event = event
    }
}

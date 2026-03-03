package com.stripe.example.fragment.event

import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.databinding.ListItemEventBinding
import com.stripe.example.model.Event

/**
 * A simple [RecyclerView.ViewHolder] that displays various events
 */
class EventHolder(
    private val binding: ListItemEventBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {
        binding.message.text = event.message
        binding.method.text = event.method
    }
}

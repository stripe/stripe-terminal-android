package com.stripe.example.fragment.discovery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.R
import com.stripe.example.databinding.ListItemReaderBinding
import com.stripe.stripeterminal.model.external.Reader

/**
 * A simple [RecyclerView.ViewHolder] that also acts as a [View.OnClickListener] to allow for
 * selecting a reader.
 */
class ReaderHolder(
    parent: ViewGroup,
    private val clickListener: ReaderClickListener,
    private val binding: ListItemReaderBinding = DataBindingUtil.inflate(
        LayoutInflater.from(parent.context), R.layout.list_item_reader, parent, false)
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(reader: Reader) {
        binding.item = reader
        binding.handler = clickListener
    }
}

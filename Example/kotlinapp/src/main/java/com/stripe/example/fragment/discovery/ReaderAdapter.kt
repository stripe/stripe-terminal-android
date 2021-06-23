package com.stripe.example.fragment.discovery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.R
import com.stripe.example.viewmodel.DiscoveryViewModel
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader

object ItemsBindingAdapter {
    @BindingAdapter("items")
    @JvmStatic
    fun RecyclerView.bindItems(items: List<Reader>) {
        val adapter = adapter as ReaderAdapter
        adapter.updateReaders(items)
    }
}

/**
 * Our [RecyclerView.Adapter] implementation that allows us to update the list of readers
 */
class ReaderAdapter(
    private val viewModel: DiscoveryViewModel,
    private val inflater: LayoutInflater,
) : RecyclerView.Adapter<ReaderHolder>() {
    private var readers: List<Reader> = viewModel.readers.value ?: listOf()
    private var locationSelection: Location? = null

    fun updateReaders(readers: List<Reader>) {
        this.readers = readers
        notifyDataSetChanged()
    }

    fun updateLocationSelection(location: Location?) {
        locationSelection = location
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return readers.size
    }

    override fun onBindViewHolder(holder: ReaderHolder, position: Int) {
        holder.bind(readers[position], locationSelection)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReaderHolder {
        return ReaderHolder(
            parent = inflater.inflate(R.layout.list_item_card, parent, false),
            clickListener = viewModel.readerClickListener!!,
        )
    }
}

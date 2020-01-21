package com.stripe.example.fragment.discovery

import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.viewmodel.DiscoveryViewModel
import com.stripe.stripeterminal.model.external.Reader

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
    private val viewModel: DiscoveryViewModel
) : RecyclerView.Adapter<ReaderHolder>() {
    private var readers: List<Reader> = viewModel.readers.value ?: listOf()

    fun updateReaders(readers: List<Reader>) {
        this.readers = readers
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return readers.size
    }

    override fun onBindViewHolder(holder: ReaderHolder, position: Int) {
        holder.bind(readers[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReaderHolder {
        return ReaderHolder(parent, viewModel.readerClickListener!!)
    }
}

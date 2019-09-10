package com.stripe.example.javaapp.fragment.discovery;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.databinding.ListItemReaderBinding;
import com.stripe.stripeterminal.model.external.Reader;

import org.jetbrains.annotations.NotNull;

/**
 * A simple [RecyclerView.ViewHolder] that also acts as a [View.OnClickListener] to allow for
 * selecting a reader.
 */
public class ReaderHolder extends RecyclerView.ViewHolder {
    @NotNull private final ReaderClickListener clickListener;
    @NotNull private final ListItemReaderBinding binding;

    public ReaderHolder(
        @NotNull ViewGroup parent,
        @NotNull ReaderClickListener clickListener
    ) {
        this(clickListener, DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.list_item_reader,
                parent,
                false));
    }

    private ReaderHolder(
        @NotNull ReaderClickListener clickListener,
        @NotNull ListItemReaderBinding binding
    ) {
        super(binding.getRoot());
        this.binding = binding;
        this.clickListener = clickListener;
    }

    void bind(@NotNull Reader reader) {
        binding.setItem(reader);
        binding.setHandler(clickListener);
        binding.executePendingBindings();
    }
}

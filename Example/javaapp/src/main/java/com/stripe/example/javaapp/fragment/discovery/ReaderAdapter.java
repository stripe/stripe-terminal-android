package com.stripe.example.javaapp.fragment.discovery;

import android.view.ViewGroup;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.viewmodel.DiscoveryViewModel;
import com.stripe.stripeterminal.model.external.Reader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Our [RecyclerView.Adapter] implementation that allows us to update the list of readers
 */
public class ReaderAdapter extends RecyclerView.Adapter<ReaderHolder> {
    @NotNull private final DiscoveryViewModel viewModel;
    @NotNull private List<? extends Reader> readers;

    @BindingAdapter("readers")
    public static void bindReaders(
            @NotNull RecyclerView recyclerView,
            @NotNull List<? extends Reader> readers
    ) {
        if (recyclerView.getAdapter() instanceof ReaderAdapter) {
            ((ReaderAdapter) recyclerView.getAdapter()).updateReaders(readers);
        }
    }

    public ReaderAdapter(@NotNull DiscoveryViewModel viewModel) {
        super();
        this.viewModel = viewModel;
        if (viewModel.readers.getValue() == null) {
            readers = new ArrayList<>();
        } else {
            readers = viewModel.readers.getValue();
        }
    }

    public void updateReaders(List<? extends Reader> readers) {
        this.readers = readers;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return readers.size();
    }

    @Override
    public void onBindViewHolder(@NotNull ReaderHolder holder, int position) {
        holder.bind(readers.get(position));
    }

    @NotNull
    @Override
    public ReaderHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        assert viewModel.readerClickListener != null;
        return new ReaderHolder(parent, viewModel.readerClickListener);
    }
}

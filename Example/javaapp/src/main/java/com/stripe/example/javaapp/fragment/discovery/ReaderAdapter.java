package com.stripe.example.javaapp.fragment.discovery;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.viewmodel.DiscoveryViewModel;
import com.stripe.stripeterminal.external.models.Location;
import com.stripe.stripeterminal.external.models.Reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Our [RecyclerView.Adapter] implementation that allows us to update the list of readers
 */
public class ReaderAdapter extends RecyclerView.Adapter<ReaderHolder> {
    @NotNull private final DiscoveryViewModel viewModel;
    @NotNull private final LayoutInflater inflater;
    @NotNull private List<? extends Reader> readers;
    @Nullable private Location locationSelection = null;

    public ReaderAdapter(@NotNull DiscoveryViewModel viewModel, LayoutInflater inflater) {
        super();
        this.inflater = inflater;
        this.viewModel = viewModel;
        if (viewModel.readers.getValue() == null) {
            readers = new ArrayList<>();
        } else {
            readers = viewModel.readers.getValue();
        }
    }

    @BindingAdapter("readers")
    public static void bindReaders(
            @NotNull RecyclerView recyclerView,
            @NotNull List<? extends Reader> readers
    ) {
        if (recyclerView.getAdapter() instanceof ReaderAdapter) {
            ((ReaderAdapter) recyclerView.getAdapter()).updateReaders(readers);
        }
    }

    public void updateLocationSelection(@Nullable Location location) {
        this.locationSelection = location;
        notifyDataSetChanged();
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
        holder.bind(readers.get(position), locationSelection);
    }

    @NotNull
    @Override
    public ReaderHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        assert viewModel.readerClickListener != null;
        return new ReaderHolder(inflater.inflate(R.layout.list_item_card, parent, false), viewModel.readerClickListener);
    }
}

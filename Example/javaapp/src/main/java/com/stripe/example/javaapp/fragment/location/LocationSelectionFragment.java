package com.stripe.example.javaapp.fragment.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.stripe.example.javaapp.MainActivity;
import com.stripe.example.javaapp.NavigationListener;
import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.recyclerview.InfiniteScrollListener;
import com.stripe.example.javaapp.viewmodel.LocationSelectionViewModel;
import com.stripe.example.javaapp.databinding.FragmentLocationSelectionBinding;

/**
 * List of locations available to select as the reader connection location.
 */
public class LocationSelectionFragment extends Fragment {
    public static final String TAG = "LocationSelectionFragment";

    private LocationSelectionViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LocationSelectionViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.reload();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        View view = inflater.inflate(R.layout.fragment_location_selection, container, false);
        FragmentLocationSelectionBinding binding = FragmentLocationSelectionBinding.bind(view);
        LocationListAdapter adapter = new LocationListAdapter(inflater, (LocationSelectionController) getActivity());

        binding.locationSelectionList.setLayoutManager(layoutManager);
        binding.locationSelectionList.addOnScrollListener(new InfiniteScrollListener(layoutManager, () -> viewModel.loadMoreLocations()));
        binding.locationSelectionList.setAdapter(adapter);
        binding.locationSelectionToolbar.inflateMenu(R.menu.location_selection);
        binding.locationSelectionToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_location_selection_add) {
                ((NavigationListener) requireActivity()).onRequestCreateLocation();
                return true;
            } else {
                return false;
            }
        });

        binding.locationSelectionCancelButton.setOnClickListener(v ->
                ((MainActivity) requireActivity()).onCancelLocationSelection()
        );

        viewModel.listState.observe(getViewLifecycleOwner(), adapter::setLocationListState);
        viewModel.error.observe(getViewLifecycleOwner(), (error) -> Toast.makeText(getActivity(), error.getErrorMessage(), Toast.LENGTH_LONG).show());

        return view;
    }

    public static LocationSelectionFragment newInstance() {
        return new LocationSelectionFragment();
    }
}

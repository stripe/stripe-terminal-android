package com.stripe.example.javaapp.fragment.discovery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.MainActivity;
import com.stripe.example.javaapp.NavigationListener;
import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.databinding.FragmentDiscoveryBinding;
import com.stripe.example.javaapp.fragment.location.LocationSelectionController;
import com.stripe.example.javaapp.viewmodel.DiscoveryViewModel;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.BluetoothReaderListener;
import com.stripe.stripeterminal.external.callable.Callback;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.DiscoveryListener;
import com.stripe.stripeterminal.external.models.BatteryStatus;
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration;
import com.stripe.stripeterminal.external.models.DiscoveryMethod;
import com.stripe.stripeterminal.external.models.Location;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage;
import com.stripe.stripeterminal.external.models.ReaderEvent;
import com.stripe.stripeterminal.external.models.ReaderInputOptions;
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate;
import com.stripe.stripeterminal.external.models.TerminalException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * The `DiscoveryFragment` shows the list of recognized readers and allows the user to
 * select one to connect to.
 */
public class DiscoveryFragment extends Fragment implements DiscoveryListener, BluetoothReaderListener, LocationSelectionController {

    public static final String TAG = "com.stripe.example.fragment.discovery.DiscoveryFragment";
    private static final String SIMULATED_KEY = "simulated";
    private static final String DISCOVERY_METHOD = "discovery_method";

    public static DiscoveryFragment newInstance(boolean simulated, DiscoveryMethod discoveryMethod) {
        final DiscoveryFragment fragment = new DiscoveryFragment();
        final Bundle bundle = new Bundle();
        bundle.putBoolean(SIMULATED_KEY, simulated);
        bundle.putSerializable(DISCOVERY_METHOD, discoveryMethod);
        fragment.setArguments(bundle);
        return fragment;
    }

    private DiscoveryViewModel viewModel;
    private ReaderAdapter adapter;
    private WeakReference<MainActivity> activityRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DiscoveryViewModelFactory discoveryViewModelFactory = new DiscoveryViewModelFactory(requireArguments());
        viewModel = new ViewModelProvider(this, discoveryViewModelFactory).get(DiscoveryViewModel.class);
        viewModel.navigationListener = (NavigationListener) getActivity();
        activityRef = new WeakReference<>((MainActivity) getActivity());
        if (viewModel.readerClickListener == null) {
            viewModel.readerClickListener = new ReaderClickListener(activityRef, viewModel);
        } else {
            viewModel.readerClickListener.setActivityRef(activityRef);
        }

        final Callback discoveryCallback = new Callback() {
            @Override
            public void onSuccess() {
                viewModel.discoveryTask = null;
            }

            @Override
            public void onFailure(@NotNull TerminalException e) {
                viewModel.discoveryTask = null;
                final MainActivity activity = activityRef.get();
                if (activity != null) {
                    activity.onCancelDiscovery();
                }
            }
        };

        if (getArguments() != null) {
            final DiscoveryConfiguration config = new DiscoveryConfiguration(
                    0, (DiscoveryMethod) getArguments().getSerializable(DISCOVERY_METHOD), getArguments().getBoolean(SIMULATED_KEY));
            if (viewModel.discoveryTask == null && Terminal.getInstance().getConnectedReader() == null) {
                viewModel.discoveryTask = Terminal
                        .getInstance()
                        .discoverReaders(config, this, discoveryCallback);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NotNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        final FragmentDiscoveryBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_discovery, container, false
        );
        binding.setLifecycleOwner(this);
        final RecyclerView readerRecyclerView = binding.getRoot().findViewById(R.id.reader_recycler_view);
        readerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.setViewModel(viewModel);
        adapter = new ReaderAdapter(viewModel, inflater);

        readerRecyclerView.setAdapter(adapter);

        binding.getRoot()
                .findViewById(R.id.cancel_button)
                .setOnClickListener(view -> {
                    if (viewModel.discoveryTask != null) {
                        viewModel.discoveryTask.cancel(new Callback() {
                            @Override
                            public void onSuccess() {
                                viewModel.discoveryTask = null;
                                final MainActivity activity = activityRef.get();
                                if (activity != null) {
                                    activity.onCancelDiscovery();
                                }
                            }

                            @Override
                            public void onFailure(@NotNull TerminalException e) {
                                viewModel.discoveryTask = null;
                            }
                        });
                    }
                });

        return binding.getRoot();
    }

    @Override
    public void onStartInstallingUpdate(@NotNull ReaderSoftwareUpdate update, @Nullable Cancelable cancelable) {
        viewModel.isConnecting.setValue(false);
        viewModel.isUpdating.setValue(false);
        viewModel.discoveryTask = cancelable;
    }

    @Override
    public void onReportReaderSoftwareUpdateProgress(float progress) {
        viewModel.updateProgress.setValue(progress);
    }

    @Override
    public void onUpdateDiscoveredReaders(@NotNull List<Reader> readers) {
        final MainActivity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(() -> viewModel.readers.setValue(readers));
        }
    }

    // Unused imports
    @Override
    public void onFinishInstallingUpdate(@Nullable ReaderSoftwareUpdate update, @Nullable TerminalException e) { }

    @Override
    public void onRequestReaderInput(@NotNull ReaderInputOptions options) { }

    @Override
    public void onRequestReaderDisplayMessage(@NotNull ReaderDisplayMessage message) { }

    @Override
    public void onReportAvailableUpdate(@NotNull ReaderSoftwareUpdate update) { }

    @Override
    public void onReportReaderEvent(@NotNull ReaderEvent event) { }

    @Override
    public void onReportLowBatteryWarning() { }

    @Override
    public void onBatteryLevelUpdate(float batteryLevel, @NonNull BatteryStatus batteryStatus, boolean isCharging) { }

    @Override
    public void onLocationSelected(Location location) {
        viewModel.selectedLocation.postValue(location);
        adapter.updateLocationSelection(location);
    }

    @Override
    public void onLocationCleared() {
        viewModel.selectedLocation.postValue(null);
        adapter.updateLocationSelection(null);
    }

    static class DiscoveryViewModelFactory implements ViewModelProvider.Factory {
        private Bundle args;

        public DiscoveryViewModelFactory(Bundle args) {
            this.args = args;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new DiscoveryViewModel((DiscoveryMethod) args.getSerializable(DISCOVERY_METHOD));
        }
    }
}

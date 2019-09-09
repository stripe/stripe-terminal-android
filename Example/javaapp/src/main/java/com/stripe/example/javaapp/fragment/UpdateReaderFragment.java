package com.stripe.example.javaapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.stripe.example.javaapp.MainActivity;
import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.databinding.FragmentUpdateReaderBinding;
import com.stripe.example.javaapp.viewmodel.UpdateReaderViewModel;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.callable.Callback;
import com.stripe.stripeterminal.callable.ReaderSoftwareUpdateCallback;
import com.stripe.stripeterminal.callable.ReaderSoftwareUpdateListener;
import com.stripe.stripeterminal.model.external.ReaderSoftwareUpdate;
import com.stripe.stripeterminal.model.external.TerminalException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * The `UpdateReaderFragment` allows the user to check the current version of the [Reader] software,
 * as well as update it when necessary.
 */
public class UpdateReaderFragment extends Fragment implements ReaderSoftwareUpdateListener {

    @NotNull public static final String TAG = "com.stripe.example.fragment.UpdateReaderFragment";

    private FragmentUpdateReaderBinding binding;
    private UpdateReaderViewModel viewModel;
    private WeakReference<MainActivity> activityRef;

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        viewModel = ViewModelProviders.of(this).get(UpdateReaderViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(
        @NotNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_update_reader, container, false);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        if (viewModel.reader == null) {
            viewModel.reader = Terminal.getInstance().getConnectedReader();
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        // Cancel update on button click
        activityRef = new WeakReference<>((MainActivity) getActivity());
        view.findViewById(R.id.cancel_button).setOnClickListener(v -> {
            if (viewModel.installOperation != null) {
                viewModel.installOperation.cancel(new Callback() {
                    @Override
                    public void onSuccess() {
                        exitWorkflow(activityRef);
                    }

                    @Override
                    public void onFailure(@NotNull TerminalException e) {
                        exitWorkflow(activityRef);
                    }
                });
            } else {
                exitWorkflow(activityRef);
            }
        });

        // We overload the "check_for_update_button" for multiple uses
        // Fetch update on button click
        view.findViewById(R.id.check_for_update_button).setOnClickListener(v -> {
            // If we haven't checked if there is an update, check
            if (!viewModel.hasStartedFetchingUpdate.getValue()) {
                viewModel.hasStartedFetchingUpdate.setValue(true);
                viewModel.fetchUpdateOperation = Terminal.getInstance().checkForUpdate(
                        new ReaderSoftwareUpdateCallback() {
                            @Override
                            public void onSuccess(@Nullable ReaderSoftwareUpdate update) {
                                final MainActivity activity = activityRef.get();
                                if (activity != null) {
                                    activity.runOnUiThread(() -> {
                                        viewModel.fetchUpdateOperation = null;
                                        onUpdateAvailable(update);
                                    });
                                }
                            }

                            @Override
                            public void onFailure(@NotNull TerminalException e) {
                                final MainActivity activity = activityRef.get();
                                if (activity != null) {
                                    activity.runOnUiThread(() -> viewModel.fetchUpdateOperation = null);
                                }
                            }
                        });
            // If we have an update ready, and we haven't installed it, do so
            } else if (viewModel.hasFinishedFetchingUpdate.getValue()) {
                final ReaderSoftwareUpdate update = viewModel.readerSoftwareUpdate.getValue();
                if (update != null) {
                    viewModel.hasStartedInstallingUpdate.setValue(true);
                    viewModel.installOperation = Terminal.getInstance().installUpdate(update, this, new Callback() {
                                @Override
                                public void onSuccess() {
                                    final MainActivity activity = activityRef.get();
                                    if (activity != null) {
                                        activity.runOnUiThread(() -> {
                                            onCompleteUpdate();
                                            viewModel.installOperation = null;
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(@NotNull TerminalException e) {
                                    final MainActivity activity = activityRef.get();
                                    if (activity != null) {
                                        activity.runOnUiThread(() -> viewModel.installOperation = null);
                                    }
                                }
                            });
                }
            }
        });

        // Done button onClick listeners
        view.findViewById(R.id.done_button).setOnClickListener(v -> exitWorkflow(activityRef));
    }

    private void onCompleteUpdate() {
        viewModel.hasFinishedInstallingUpdate.setValue(true);
    }

    private void onUpdateAvailable(@Nullable ReaderSoftwareUpdate update) {
        viewModel.readerSoftwareUpdate.setValue(update);
        viewModel.hasFinishedFetchingUpdate.setValue(true);
    }

    @Override
    public void onReportReaderSoftwareUpdateProgress(float progress) {
        final MainActivity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(() -> viewModel.progress.setValue((double) progress));
        }
    }

    private void exitWorkflow(@NotNull WeakReference<MainActivity> activityRef) {
        final MainActivity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(activity::onRequestExitWorkflow);
        }
    }
}

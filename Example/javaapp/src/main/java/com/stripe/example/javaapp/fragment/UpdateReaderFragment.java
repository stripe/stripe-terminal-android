package com.stripe.example.javaapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.button.MaterialButton;
import com.stripe.example.javaapp.MainActivity;
import com.stripe.example.javaapp.R;
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
import java.util.Objects;

/**
 * The `UpdateReaderFragment` allows the user to check the current version of the [Reader] software,
 * as well as update it when necessary.
 */
public class UpdateReaderFragment extends Fragment implements ReaderSoftwareUpdateListener {

    @NotNull public static final String TAG = "com.stripe.example.fragment.UpdateReaderFragment";

    private UpdateReaderViewModel viewModel;
    private WeakReference<MainActivity> activityRef;

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        viewModel = ViewModelProviders.of(this).get(UpdateReaderViewModel.class);
        if (viewModel.reader == null) {
            viewModel.reader = Terminal.getInstance().getConnectedReader();
        }
    }

    @Override
    @Nullable
    public View onCreateView(
        @NotNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_update_reader, container, false);
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

        viewModel.doneButtonVisibility.observe(this, visibility -> {
            final TextView textView = view.findViewById(R.id.cancel_button);
            textView.setTextColor(ContextCompat.getColor(
                    Objects.requireNonNull(getContext()),
                    visibility ? R.color.colorPrimaryDark : R.color.colorAccent));
            view.findViewById(R.id.done_button)
                    .setVisibility(visibility ? View.VISIBLE : View.GONE);
        });

        ((TextView) view.findViewById(R.id.reader_description)).setText(
                Objects.requireNonNull(getContext()).getString(
                        R.string.reader_description,
                        Objects.requireNonNull(viewModel.reader).getDeviceType().name(),
                        viewModel.reader.getSerialNumber()));

        ((MaterialButton) view.findViewById(R.id.current_version)).setText(
                viewModel.reader.getSoftwareVersion());

        viewModel.checkForUpdateButtonVisibility.observe(this, visibility ->
            view.findViewById(R.id.check_for_update_description)
                    .setVisibility(visibility ? View.VISIBLE : View.GONE));

        viewModel.checkForUpdateButtonText.observe(this, text ->
            ((MaterialButton) view.findViewById(R.id.check_for_update_button)).setText(text));

        viewModel.checkForUpdateButtonColor.observe(this, color ->
            ((MaterialButton) view.findViewById(R.id.check_for_update_button)).setTextColor(color));

        viewModel.checkForUpdateDescriptionText.observe(this, text ->
                ((TextView) view.findViewById(R.id.check_for_update_description)).setText(text));

        viewModel.checkForUpdateDescriptionVisibility.observe(this, visibility ->
                view.findViewById(R.id.check_for_update_description)
                        .setVisibility(visibility ? View.VISIBLE : View.GONE));

        viewModel.installDisclaimerVisibility.observe(this, visibility ->
                view.findViewById(R.id.install_disclaimer)
                        .setVisibility(visibility ? View.VISIBLE : View.GONE));
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

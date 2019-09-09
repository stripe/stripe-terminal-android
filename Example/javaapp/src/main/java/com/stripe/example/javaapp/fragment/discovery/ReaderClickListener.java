package com.stripe.example.javaapp.fragment.discovery;

import com.stripe.example.javaapp.MainActivity;
import com.stripe.example.javaapp.viewmodel.DiscoveryViewModel;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.callable.ReaderCallback;
import com.stripe.stripeterminal.model.external.Reader;
import com.stripe.stripeterminal.model.external.TerminalException;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class ReaderClickListener {
    @NotNull private WeakReference<MainActivity> activityRef;
    @NotNull private final DiscoveryViewModel viewModel;

    ReaderClickListener(
        @NotNull WeakReference<MainActivity> activityRef,
        @NotNull DiscoveryViewModel viewModel
    ) {
        this.activityRef = activityRef;
        this.viewModel = viewModel;
    }

    public void setActivityRef(@NotNull WeakReference<MainActivity> newRef) {
        activityRef = newRef;
    }

    public void onClick(@NotNull Reader reader) {
        viewModel.isConnecting.setValue(true);
        Terminal.getInstance().connectReader(reader, new ReaderCallback() {
            @Override
            public void onSuccess(@NotNull Reader reader) {
                final MainActivity activity = activityRef.get();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        viewModel.isConnecting.setValue(false);
                        activity.onConnectReader();
                    });
                }
            }

            @Override
            public void onFailure(@NotNull TerminalException e) {
                final MainActivity activity = activityRef.get();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        viewModel.isConnecting.setValue(false);
                        activity.onCancelDiscovery();
                    });
                }
            }
        });
    }
}

package com.stripe.example.javaapp.fragment.discovery;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.stripe.example.javaapp.MainActivity;
import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.viewmodel.DiscoveryViewModel;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.ReaderCallback;
import com.stripe.stripeterminal.external.models.ConnectionConfiguration.BluetoothConnectionConfiguration;
import com.stripe.stripeterminal.external.models.Location;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.TerminalException;

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
        MainActivity activity = activityRef.get();
        if (activity == null) return;
        Location selectedLocation = viewModel.selectedLocation.getValue();
        Location readerLocation = reader.getLocation();
        String connectLocationId = null;

        if (selectedLocation != null) {
            connectLocationId = selectedLocation.getId();
        } else if (readerLocation != null) {
            connectLocationId = readerLocation.getId();
        } else {
            new AlertDialog.Builder(activity)
                .setPositiveButton(R.string.alert_acknowledge_button, (DialogInterface.OnClickListener) (dialog, which) -> {})
                .setTitle(R.string.location_required_dialog_title)
                .setMessage(R.string.location_required_dialog_message)
                .show();
            return;
        }

        viewModel.isConnecting.setValue(true);
        Terminal.getInstance().connectBluetoothReader(reader, new BluetoothConnectionConfiguration(connectLocationId),
                activityRef.get(), new ReaderCallback() {
            @Override
            public void onSuccess(@NotNull Reader reader) {
                final MainActivity activity = activityRef.get();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        viewModel.isConnecting.setValue(false);
                        viewModel.isUpdating.setValue(false);
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
                        viewModel.isUpdating.setValue(false);
                        activity.onCancelDiscovery();
                    });
                }
            }
        });
    }
}

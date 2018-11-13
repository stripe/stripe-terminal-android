package com.stripe.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.collect.Maps;
import com.stripe.stripeterminal.Callback;
import com.stripe.stripeterminal.DiscoveryConfiguration;
import com.stripe.stripeterminal.DiscoveryListener;
import com.stripe.stripeterminal.Reader;
import com.stripe.stripeterminal.ReaderCallback;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.TerminalException;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The {@code ReaderFragment} is where the user is able to connect and disconnect from readers.
 * The fragment will also show if any readers are currently connected to.
 */
public class ReaderFragment extends Fragment implements OnRequestPermissionsResultCallback {

    // The code that denotes when a reader was selected for connection
    private static final int REQUEST_CODE_SELECTED_READER = 0;

    private TextView connectionStatus;
    private Button discoverButton;
    private Button disconnectButton;

    private Terminal terminal;

    // Keep a map of all discovered readers and their names
    private Map<String, Reader> discoveredReaders = Maps.newHashMap();

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }

        // If a reader has been selected, connect to that reader
        if (requestCode == REQUEST_CODE_SELECTED_READER) {
            String readerSelectionName = ReaderSelectionActivity.getReaderSelection(data);
            if (discoveredReaders.containsKey(readerSelectionName)) {
                terminal.connectReader(discoveredReaders.get(readerSelectionName), new ConnectionCallback());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reader, container, false);

        // Initialize the BBPOSConnector on creation
        terminal = TerminalProvider.getInstance(getActivity());

        // Grab connection status to update later
        connectionStatus = view.findViewById(R.id.connection_status);
        if (terminal.getConnectedReader() != null) {
            setStatusText(terminal.getConnectedReader().getSerialNumber());
        } else {
            setStatusText(getString(R.string.not_connected));
        }

        // Set click listener to find devices
        discoverButton = view.findViewById(R.id.discover_button);
        discoverButton.setOnClickListener(v -> {
            terminal.discoverReaders(new DiscoveryConfiguration(), new ReaderDiscoveryListener(),
                    new DiscoveryCallback());
            Intent intent = new Intent(getActivity(), ReaderSelectionActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SELECTED_READER);
        });

        // Set click listener to disconnectReader from all devices
        disconnectButton = view.findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener(v -> terminal.disconnectReader(new DisconnectionCallback()));

        return view;
    }

    /**
     * A simple callback to be called once the discovery process has completed
     */
    private class DiscoveryCallback implements Callback {

        @Override
        public void onSuccess() {
            // No need to do anything here. Next steps are handled in the connection callback
        }

        @Override
        public void onFailure(@Nonnull TerminalException e) {
            Log.e(getClass().getSimpleName(), e.getErrorMessage(), e);
        }
    }

    /**
     * A simple listener that will update the list of readers whenever new readers are found
     */
    private class ReaderDiscoveryListener implements DiscoveryListener {

        @Override
        public void onUpdateDiscoveredReaders(List<Reader> readers) {
            readers.forEach(reader -> discoveredReaders.put(reader.getSerialNumber(), reader));
            ReaderList.update(discoveredReaders.keySet().toArray(new String[0]));
        }
    }

    /**
     * A simple callback to be run after connection has completed
     */
    private class ConnectionCallback implements ReaderCallback {

        @Override
        public void onSuccess(Reader reader) {
            getActivity().runOnUiThread(() -> setStatusText(reader.getSerialNumber()));
        }

        @Override
        public void onFailure(@Nonnull TerminalException e) {
            Log.e(getClass().getSimpleName(), e.getErrorMessage(), e);
        }
    }

    /**
     * A simple callback to be run after disconnect has completed
     */
    private class DisconnectionCallback implements Callback {

        @Override
        public void onSuccess() {
            getActivity().runOnUiThread(() ->setStatusText(getString(R.string.not_connected)));
        }

        @Override
        public void onFailure(@Nonnull TerminalException e) {
            Log.e(getClass().getSimpleName(), e.getErrorMessage(), e);
        }
    }

    // A private helper to easily set the connection status text
    private void setStatusText(String currentStatus) {
        connectionStatus.setText(String.format(getString(R.string.connection_status),
                currentStatus));
    }
}

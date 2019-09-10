package com.stripe.example.javaapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.stripe.example.javaapp.NavigationListener;
import com.stripe.example.javaapp.R;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.callable.Callback;
import com.stripe.stripeterminal.model.external.Reader;
import com.stripe.stripeterminal.model.external.TerminalException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * The `ConnectedReaderFragment` displays the reader that's currently connected and provides
 * options for workflows that can be executed.
 */
public class ConnectedReaderFragment extends Fragment {

    @NotNull public static final String TAG = "com.stripe.example.fragment.ConnectedReaderFragment";

    @Override
    @Nullable
    public View onCreateView(
            @NotNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_connected_reader, container, false);

        // Set the description of the connected reader
        final Reader connectedReader = Terminal.getInstance().getConnectedReader();
        if (connectedReader != null) {
            // TODO: Set status as well
            final String description = getString(R.string.reader_description, connectedReader.getDeviceType(), connectedReader.getSerialNumber());
            ((TextView) view.findViewById(R.id.reader_description)).setText(description);
        }

        // Set up the disconnect button
        final WeakReference<FragmentActivity> activityRef = new WeakReference<>(getActivity());
        view.findViewById(R.id.disconnect_button).setOnClickListener(v ->
                Terminal.getInstance().disconnectReader(new Callback() {
                    @Override
                    public void onSuccess() {
                        final FragmentActivity activity = activityRef.get();
                        if (activity != null) {
                            if (activity instanceof NavigationListener) {
                                ((NavigationListener) activity).onDisconnectReader();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull TerminalException e) {
                    }
                })
        );

        // Set up the collect payment button
        view.findViewById(R.id.collect_card_payment_button).setOnClickListener(v -> {
            final FragmentActivity activity = getActivity();
            if (activity instanceof NavigationListener) {
                ((NavigationListener) activity).onSelectPaymentWorkflow();
            }
        });

        // Set up the read reusable card button
        view.findViewById(R.id.read_reusable_card_button).setOnClickListener(v -> {
            final FragmentActivity activity = getActivity();
            if (activity instanceof NavigationListener) {
                ((NavigationListener) activity).onSelectReadReusableCardWorkflow();
            }
        });

        // Set up the update reader button
        view.findViewById(R.id.update_reader_button).setOnClickListener(v -> {
            final FragmentActivity activity = getActivity();
            if (activity instanceof NavigationListener) {
                ((NavigationListener) activity).onSelectUpdateWorkflow();
            }
        });

        return view;
    }
}

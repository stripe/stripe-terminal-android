package com.stripe.example.javaapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.stripe.example.javaapp.NavigationListener;
import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.TerminalOfflineListener;
import com.stripe.example.javaapp.customviews.TerminalOnlineIndicator;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.OfflineMode;
import com.stripe.stripeterminal.external.callable.Callback;
import com.stripe.stripeterminal.external.callable.OfflineListener;
import com.stripe.stripeterminal.external.models.OfflineStatus;
import com.stripe.stripeterminal.external.models.PaymentIntent;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.TerminalException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * The `ConnectedReaderFragment` displays the reader that's currently connected and provides
 * options for workflows that can be executed.
 */
@OptIn(markerClass = OfflineMode.class)
public class ConnectedReaderFragment extends Fragment implements OfflineListener {

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
                                activity.runOnUiThread(
                                    ((NavigationListener) activity)::onDisconnectReader
                                );
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

        // Set up the setup intent button
        view.findViewById(R.id.save_card_button).setOnClickListener(v -> {
            final FragmentActivity activity = getActivity();
            if (activity instanceof NavigationListener) {
                ((NavigationListener) activity).onRequestSaveCard();
            }
        });

        // Set up the update reader button
        view.findViewById(R.id.update_reader_button).setOnClickListener(v -> {
            final FragmentActivity activity = getActivity();
            if (activity instanceof NavigationListener) {
                ((NavigationListener) activity).onSelectUpdateWorkflow();
            }
        });

        // Set up the view offline logs button
        view.findViewById(R.id.view_offline_logs_button).setOnClickListener(v -> {
            final FragmentActivity activity = getActivity();
            if (activity instanceof NavigationListener) {
                ((NavigationListener) activity).onSelectViewOfflineLogs();
            }
        });

        onOfflineStatusChange(Terminal.getInstance().getOfflineStatus());
        TerminalOfflineListener.instance.addListener(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TerminalOfflineListener.instance.removeListener(this);
    }

    @Override
    public void onOfflineStatusChange(@NonNull OfflineStatus offlineStatus) {
        View view = getView();
        if (view != null) {
            TerminalOnlineIndicator indicator = view.findViewById(R.id.online_indicator);
            indicator.setNetworkStatus(offlineStatus.getSdk().getNetworkStatus());
        }
    }

    @Override
    public void onPaymentIntentForwarded(@NonNull PaymentIntent paymentIntent, @androidx.annotation.Nullable TerminalException e) {
        // no-op
    }

    @Override
    public void onForwardingFailure(@NonNull TerminalException e) {
        // no-op
    }
}

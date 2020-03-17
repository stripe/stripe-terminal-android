package com.stripe.example.javaapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.stripe.example.javaapp.fragment.ConnectedReaderFragment;
import com.stripe.example.javaapp.fragment.PaymentFragment;
import com.stripe.example.javaapp.fragment.TerminalFragment;
import com.stripe.example.javaapp.fragment.UpdateReaderFragment;
import com.stripe.example.javaapp.fragment.event.EventFragment;
import com.stripe.example.javaapp.fragment.discovery.DiscoveryFragment;
import com.stripe.example.javaapp.network.ApiClient;
import com.stripe.example.javaapp.network.TokenProvider;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.TerminalLifecycleObserver;
import com.stripe.stripeterminal.log.LogLevel;
import com.stripe.stripeterminal.model.external.ConnectionStatus;
import com.stripe.stripeterminal.model.external.TerminalException;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity implements NavigationListener {

    private static final int REQUEST_CODE_LOCATION = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Check that the example app has been configured correctly
        if (ApiClient.BACKEND_URL.isEmpty()) {
            throw new RuntimeException(
                    "You need to set the BACKEND_URL constant in ApiClient.java " +
                    "before you'll be able to use the example app.");
        }

        if (BluetoothAdapter.getDefaultAdapter() != null &&
                !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!Terminal.isInitialized() && verifyGpsEnabled()) {
                initialize();
            }
        } else {
            // If we don't have them yet, request them before doing anything else
            final String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION };
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Receive the result of our permissions check, and initialize if we can
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NotNull String[] permissions,
            @NotNull int[] grantResults
    ) {
        // If we receive a response to our permission check, initialize
        if (requestCode == REQUEST_CODE_LOCATION && !Terminal.isInitialized() && verifyGpsEnabled()) {
            initialize();
        }
    }

    // Navigation callbacks

    /**
     * Callback function called when discovery has been canceled by the [DiscoveryFragment]
     */
    @Override
    public void onCancelDiscovery() {
        navigateTo(TerminalFragment.TAG, new TerminalFragment());
    }

    /**
     * Callback function called once discovery has been selected by the [TerminalFragment]
     */
    @Override
    public void onRequestDiscovery(boolean isSimulated) {
        navigateTo(DiscoveryFragment.TAG, DiscoveryFragment.newInstance(isSimulated));
    }

    /**
     * Callback function called to exit the payment workflow
     */
    @Override
    public void onRequestExitWorkflow() {
        if (Terminal.getInstance().getConnectionStatus() == ConnectionStatus.CONNECTED) {
            navigateTo(ConnectedReaderFragment.TAG, new ConnectedReaderFragment());
        } else {
            navigateTo(TerminalFragment.TAG, new TerminalFragment());
        }
    }

    /**
     * Callback function called to start a payment by the [PaymentFragment]
     */
    @Override
    public void onRequestPayment(int amount, @NotNull String currency) {
        navigateTo(EventFragment.TAG, EventFragment.requestPayment(amount, currency));
    }

    /**
     * Callback function called once the payment workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    @Override
    public void onSelectPaymentWorkflow() {
        navigateTo(PaymentFragment.TAG, new PaymentFragment());
    }

    /**
     * Callback function called once the read card workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    @Override
    public void onSelectReadReusableCardWorkflow() {
        navigateTo(EventFragment.TAG, EventFragment.readReusableCard());
    }

    /**
     * Callback function called once the update reader workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    @Override
    public void onSelectUpdateWorkflow() {
        navigateTo(UpdateReaderFragment.TAG, new UpdateReaderFragment());
    }

    // Terminal event callbacks

    /**
     * Callback function called when collect payment method has been canceled
     */
    @Override
    public void onCancelCollectPaymentMethod() {
        navigateTo(ConnectedReaderFragment.TAG, new ConnectedReaderFragment());
    }

    /**
     * Callback function called on completion of [Terminal.connectReader]
     */
    @Override
    public void onConnectReader() {
        navigateTo(ConnectedReaderFragment.TAG, new ConnectedReaderFragment());
    }

    @Override
    public void onDisconnectReader() {
        navigateTo(TerminalFragment.TAG, new TerminalFragment());
    }

    /**
     * Initialize the [Terminal] and go to the [TerminalFragment]
     */
    private void initialize() {
        // Initialize the Terminal as soon as possible
        try {
            Terminal.initTerminal(getApplicationContext(), LogLevel.VERBOSE, new TokenProvider(),
                    new TerminalEventListener());
        } catch (TerminalException e) {
            throw new RuntimeException("Location services are required in order to initialize " +
                    "the Terminal.", e);
        }

        navigateTo(TerminalFragment.TAG, new TerminalFragment());
    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment Fragment to navigate to.
     */
    private void navigateTo(String tag, Fragment fragment) {
        final Fragment frag = getSupportFragmentManager().findFragmentByTag(tag);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, frag != null ? frag : fragment, tag)
                .commitAllowingStateLoss();
    }

    private boolean verifyGpsEnabled() {
        final LocationManager locationManager =
                (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        boolean gpsEnabled = false;
        try {
            gpsEnabled = locationManager != null &&
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception exception) {
        }

        if (!gpsEnabled) {
            // notify user
            new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_MaterialComponents_DayNight_DarkActionBar))
                    .setMessage("Please enable location services")
                    .setCancelable(false)
                    .setPositiveButton("Open location settings", (dialog, which) -> {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .create()
                    .show();
        }

        return gpsEnabled;
    }
}

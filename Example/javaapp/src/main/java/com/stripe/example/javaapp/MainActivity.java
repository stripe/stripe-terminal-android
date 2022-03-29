package com.stripe.example.javaapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.stripe.example.javaapp.fragment.ConnectedReaderFragment;
import com.stripe.example.javaapp.fragment.PaymentFragment;
import com.stripe.example.javaapp.fragment.TerminalFragment;
import com.stripe.example.javaapp.fragment.UpdateReaderFragment;
import com.stripe.example.javaapp.fragment.discovery.DiscoveryFragment;
import com.stripe.example.javaapp.fragment.event.EventFragment;
import com.stripe.example.javaapp.fragment.location.LocationCreateFragment;
import com.stripe.example.javaapp.fragment.location.LocationSelectionController;
import com.stripe.example.javaapp.fragment.location.LocationSelectionFragment;
import com.stripe.example.javaapp.network.ApiClient;
import com.stripe.example.javaapp.network.TokenProvider;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.BluetoothReaderListener;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.UsbReaderListener;
import com.stripe.stripeterminal.external.models.BatteryStatus;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.DiscoveryMethod;
import com.stripe.stripeterminal.external.models.Location;
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage;
import com.stripe.stripeterminal.external.models.ReaderEvent;
import com.stripe.stripeterminal.external.models.ReaderInputOptions;
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate;
import com.stripe.stripeterminal.external.models.TerminalException;
import com.stripe.stripeterminal.log.LogLevel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        NavigationListener,
        BluetoothReaderListener,
        UsbReaderListener,
        LocationSelectionController {

    // Register the permissions callback to handles the response to the system permissions dialog.
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            this::onActivityResult
    );

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

        requestPermissionsIfNecessary();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED
        ) {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && !adapter.isEnabled()) {
                adapter.enable();
            }
        } else {
            Log.w(getClass().getSimpleName(), "Failed to acquire Bluetooth permission");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requestPermissionsIfNecessary();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void requestPermissionsIfNecessary() {
        if (Build.VERSION.SDK_INT >= 31) {
            requestPermissionsIfNecessarySdk31();
        } else {
            requestPermissionsIfNecessarySdkBelow31();
        }
    }

    private boolean isGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionsIfNecessarySdkBelow31() {
        // Check for location permissions
        if (!isGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // If we don't have them yet, request them before doing anything else
            final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissionLauncher.launch(permissions);
        } else if (!Terminal.isInitialized() && verifyGpsEnabled()) {
            initialize();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestPermissionsIfNecessarySdk31() {
        // Check for location and bluetooth permissions
        List<String> deniedPermissions = new ArrayList<>();
        if (!isGranted(Manifest.permission.ACCESS_FINE_LOCATION))
            deniedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (!isGranted(Manifest.permission.BLUETOOTH_CONNECT))
            deniedPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        if (!isGranted(Manifest.permission.BLUETOOTH_SCAN))
            deniedPermissions.add(Manifest.permission.BLUETOOTH_SCAN);

        if (!deniedPermissions.isEmpty()) {
            // If we don't have them yet, request them before doing anything else
            String[] deniedPermissionsArray = new String[deniedPermissions.size()];
            deniedPermissionsArray = deniedPermissions.toArray(deniedPermissionsArray);
            requestPermissionLauncher.launch(deniedPermissionsArray);
        } else if (!Terminal.isInitialized() && verifyGpsEnabled()) {
            initialize();
        }
    }

    /**
     * Receive the result of our permissions check, and initialize if we can
     */
    private void onActivityResult(Map<String, Boolean> result) {
        List<String> deniedPermissions = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            if (!entry.getValue()) {
                deniedPermissions.add(entry.getKey());
            }
        }

        if (deniedPermissions.isEmpty() && !Terminal.isInitialized() && verifyGpsEnabled()) {
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

    @Override
    public void onRequestChangeLocation() {
        navigateTo(
                LocationSelectionFragment.TAG,
                LocationSelectionFragment.newInstance(),
                false,
                true
        );
    }

    @Override
    public void onRequestCreateLocation() {
        navigateTo(LocationCreateFragment.TAG, LocationCreateFragment.newInstance(), false, true);
    }

    @Override
    public void onLocationCreated() {
        getSupportFragmentManager().popBackStackImmediate();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        ((LocationSelectionFragment) fragments.get(fragments.size() - 1)).reload();
    }

    /**
     * Callback function called once discovery has been selected by the [TerminalFragment]
     */
    @Override
    public void onRequestDiscovery(boolean isSimulated, DiscoveryMethod discoveryMethod) {
        navigateTo(DiscoveryFragment.TAG, DiscoveryFragment.newInstance(isSimulated, discoveryMethod));
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
    public void onRequestPayment(long amount, @NotNull String currency, boolean skipTipping, boolean extendedAuth, boolean incrementalAuth) {
        navigateTo(EventFragment.TAG, EventFragment.requestPayment(amount, currency, skipTipping, extendedAuth, incrementalAuth));
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

    @Override
    public void onStartInstallingUpdate(@NotNull ReaderSoftwareUpdate update, @Nullable Cancelable cancelable) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof BluetoothReaderListener) {
                ((BluetoothReaderListener) currentFragment).onStartInstallingUpdate(update, cancelable);
            }
        });
    }

    @Override
    public void onReportReaderSoftwareUpdateProgress(float progress) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof BluetoothReaderListener) {
                ((BluetoothReaderListener) currentFragment).onReportReaderSoftwareUpdateProgress(progress);
            }
        });
    }

    @Override
    public void onFinishInstallingUpdate(@Nullable ReaderSoftwareUpdate update, @Nullable TerminalException e) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof BluetoothReaderListener) {
                ((BluetoothReaderListener) currentFragment).onFinishInstallingUpdate(update, e);
            }
        });
    }

    @Override
    public void onRequestReaderInput(@NotNull ReaderInputOptions options) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof BluetoothReaderListener) {
                ((BluetoothReaderListener) currentFragment).onRequestReaderInput(options);
            }
        });
    }

    @Override
    public void onRequestReaderDisplayMessage(@NotNull ReaderDisplayMessage message) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof BluetoothReaderListener) {
                ((BluetoothReaderListener) currentFragment).onRequestReaderDisplayMessage(message);
            }
        });
    }

    @Override
    public void onReportAvailableUpdate(@NotNull ReaderSoftwareUpdate update) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof BluetoothReaderListener) {
                ((BluetoothReaderListener) currentFragment).onReportAvailableUpdate(update);
            }
        });
    }

    @Override
    public void onReportReaderEvent(@NotNull ReaderEvent event) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof BluetoothReaderListener) {
                ((BluetoothReaderListener) currentFragment).onReportReaderEvent(event);
            }
        });
    }

    @Override
    public void onReportLowBatteryWarning() {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof BluetoothReaderListener) {
                ((BluetoothReaderListener) currentFragment).onReportLowBatteryWarning();
            }
        });
    }

    @Override
    public void onBatteryLevelUpdate(float batteryLevel, @NonNull BatteryStatus batteryStatus, boolean isCharging) { }

    @Override
    public void onLocationSelected(Location location) {
        getSupportFragmentManager().popBackStackImmediate();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Fragment lastFragment = fragments.get(fragments.size() - 1);
        if (lastFragment instanceof LocationSelectionController) {
            ((LocationSelectionController) lastFragment).onLocationSelected(location);
        }
    }

    @Override
    public void onLocationCleared() {
        getSupportFragmentManager().popBackStackImmediate();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Fragment lastFragment = fragments.get(fragments.size() - 1);
        if (lastFragment instanceof LocationSelectionController) {
            ((LocationSelectionController) lastFragment).onLocationCleared();
        }
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
        navigateTo(tag, fragment, true, false);
    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment Fragment to navigate to.
     */
    private void navigateTo(String tag, Fragment fragment, boolean replace, boolean addToBackstack) {
        final Fragment frag = getSupportFragmentManager().findFragmentByTag(tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (replace) {
            transaction.replace(R.id.container, frag != null ? frag : fragment, tag);
        } else {
            transaction.add(R.id.container, fragment, tag);
        }

        if (addToBackstack) {
            transaction.addToBackStack(tag);
        }

        transaction.commitAllowingStateLoss();
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

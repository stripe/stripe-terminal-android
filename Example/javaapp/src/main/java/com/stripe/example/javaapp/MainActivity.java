package com.stripe.example.javaapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.stripe.example.javaapp.fragment.ConnectedReaderFragment;
import com.stripe.example.javaapp.fragment.PaymentFragment;
import com.stripe.example.javaapp.fragment.TerminalFragment;
import com.stripe.example.javaapp.fragment.UpdateReaderFragment;
import com.stripe.example.javaapp.fragment.discovery.DiscoveryFragment;
import com.stripe.example.javaapp.fragment.discovery.DiscoveryMethod;
import com.stripe.example.javaapp.fragment.event.EventFragment;
import com.stripe.example.javaapp.fragment.location.LocationCreateFragment;
import com.stripe.example.javaapp.fragment.location.LocationSelectionController;
import com.stripe.example.javaapp.fragment.location.LocationSelectionFragment;
import com.stripe.example.javaapp.fragment.offline.OfflinePaymentsLogFragment;
import com.stripe.example.javaapp.model.OfflineBehaviorSelection;
import com.stripe.example.javaapp.network.TokenProvider;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.OfflineMode;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.InternetReaderListener;
import com.stripe.stripeterminal.external.callable.MobileReaderListener;
import com.stripe.stripeterminal.external.callable.TapToPayReaderListener;
import com.stripe.stripeterminal.external.models.BatteryStatus;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.DisconnectReason;
import com.stripe.stripeterminal.external.models.Location;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage;
import com.stripe.stripeterminal.external.models.ReaderEvent;
import com.stripe.stripeterminal.external.models.ReaderInputOptions;
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate;
import com.stripe.stripeterminal.external.models.TerminalException;
import com.stripe.stripeterminal.log.LogLevel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@OptIn(markerClass = OfflineMode.class)
public class MainActivity extends AppCompatActivity implements
        NavigationListener,
        MobileReaderListener,
        TapToPayReaderListener,
        InternetReaderListener,
        LocationSelectionController
{

    public final OfflineModeHandler offlineModeHandler = new OfflineModeHandler(message -> {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

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

        initialize();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
    public void onRequestLocationSelection() {
        navigateTo(
                LocationSelectionFragment.TAG,
                LocationSelectionFragment.newInstance(),
                false,
                true
        );
    }

    @Override
    public void onCancelLocationSelection() {
        getSupportFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onRequestCreateLocation() {
        navigateTo(LocationCreateFragment.TAG, LocationCreateFragment.newInstance(), false, true);
    }

    @Override
    public void onCancelCreateLocation() {
        getSupportFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onLocationCreated() {
        getSupportFragmentManager().popBackStackImmediate();
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
    public void onRequestPayment(long amount, @NotNull String currency, boolean skipTipping, boolean extendedAuth, boolean incrementalAuth, OfflineBehaviorSelection offlineBehaviorSelection) {
        navigateTo(EventFragment.TAG, EventFragment.requestPayment(amount, currency, skipTipping, extendedAuth, incrementalAuth, offlineBehaviorSelection));
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
    public void onRequestSaveCard() {
        navigateTo(EventFragment.TAG, EventFragment.collectSetupIntentPaymentMethod());
    }

    /**
     * Callback function called once the update reader workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    @Override
    public void onSelectUpdateWorkflow() {
        navigateTo(UpdateReaderFragment.TAG, new UpdateReaderFragment());
    }

    /**
     * Callback function called once the view offline logs has been selected by the
     * [ConnectedReaderFragment]
     */
    @Override
    public void onSelectViewOfflineLogs() {
        navigateTo(OfflinePaymentsLogFragment.TAG, new OfflinePaymentsLogFragment());
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
     * Callback function called when collect setup intent has been canceled
     */
    @Override
    public void onCancelCollectSetupIntent() {
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
            if (currentFragment instanceof MobileReaderListener) {
                ((MobileReaderListener) currentFragment).onStartInstallingUpdate(update, cancelable);
            }
        });
    }

    @Override
    public void onReportReaderSoftwareUpdateProgress(float progress) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof MobileReaderListener) {
                ((MobileReaderListener) currentFragment).onReportReaderSoftwareUpdateProgress(progress);
            }
        });
    }

    @Override
    public void onFinishInstallingUpdate(@Nullable ReaderSoftwareUpdate update, @Nullable TerminalException e) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof MobileReaderListener) {
                ((MobileReaderListener) currentFragment).onFinishInstallingUpdate(update, e);
            }
        });
    }

    @Override
    public void onRequestReaderInput(@NotNull ReaderInputOptions options) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof MobileReaderListener) {
                ((MobileReaderListener) currentFragment).onRequestReaderInput(options);
            }
        });
    }

    @Override
    public void onRequestReaderDisplayMessage(@NotNull ReaderDisplayMessage message) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof MobileReaderListener) {
                ((MobileReaderListener) currentFragment).onRequestReaderDisplayMessage(message);
            }
        });
    }

    @Override
    public void onReportAvailableUpdate(@NotNull ReaderSoftwareUpdate update) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof MobileReaderListener) {
                ((MobileReaderListener) currentFragment).onReportAvailableUpdate(update);
            }
        });
    }

    @Override
    public void onReportReaderEvent(@NotNull ReaderEvent event) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof MobileReaderListener) {
                ((MobileReaderListener) currentFragment).onReportReaderEvent(event);
            }
        });
    }

    @Override
    public void onReportLowBatteryWarning() {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof MobileReaderListener) {
                ((MobileReaderListener) currentFragment).onReportLowBatteryWarning();
            }
        });
    }

    @Override
    public void onBatteryLevelUpdate(float batteryLevel, @NonNull BatteryStatus batteryStatus, boolean isCharging) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof MobileReaderListener) {
                ((MobileReaderListener) currentFragment).onBatteryLevelUpdate(batteryLevel, batteryStatus, isCharging);
            }
        });
    }

    @Override
    public void onDisconnect(@NonNull DisconnectReason reason) {
        runOnUiThread(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Fragment currentFragment = fragments.get(fragments.size() - 1);
            if (currentFragment instanceof MobileReaderListener) {
                ((MobileReaderListener) currentFragment).onDisconnect(reason);
            }
        });
    }

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
     * ReaderReconnectionListener implementation.
     */
    @Override
    public void onReaderReconnectStarted(@NonNull Reader reader, @NonNull Cancelable cancelReconnect, @NonNull DisconnectReason reason) {
        Log.d("MainActivity", "Reconnection to reader " + reader.getId() + " started!");
    }
    @Override
    public void onReaderReconnectSucceeded(@NonNull Reader reader) {
        Log.d("MainActivity", "Reader " + reader.getId() + " reconnected successfully!");
    }

    @Override
    public void onReaderReconnectFailed(@NonNull Reader reader) {
        Log.d("MainActivity", "Reconnection to reader " + reader.getId() + " failed!");
    }

    /**
     * Initialize the [Terminal] and go to the [TerminalFragment]
     */
    private void initialize() {
        // Initialize the Terminal as soon as possible
        try {
            if (!Terminal.isInitialized()) {
                Terminal.initTerminal(getApplicationContext(), LogLevel.VERBOSE, new TokenProvider(),
                        TerminalEventListener.instance, TerminalOfflineListener.instance);
            }
        } catch (TerminalException e) {
            throw new RuntimeException(e);
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
}

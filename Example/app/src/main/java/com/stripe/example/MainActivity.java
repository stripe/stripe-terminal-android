package com.stripe.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.stripe.exception.StripeException;
import com.stripe.stripeterminal.ConnectionTokenCallback;
import com.stripe.stripeterminal.ConnectionTokenException;
import com.stripe.stripeterminal.ConnectionTokenProvider;
import com.stripe.stripeterminal.LogLevel;
import com.stripe.stripeterminal.Terminal;

import javax.annotation.Nullable;

/**
 * The {@code MainActivity} is the starting activity for the example app. It will show the
 * {@link ReaderFragment} as default.
 */
public class MainActivity extends AppCompatActivity {

    // The code that denotes the request for location permissions
    private static final int REQUEST_CODE_LOCATION = 1;

    private BottomNavigationView bottomNavigationView;
    private Fragment fragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // We need to ask for location permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION);
        }

        // Initialize the Terminal as soon as possible
        Terminal.initTerminal(getApplicationContext(), LogLevel.VERBOSE, new TokenProvider(),
                new TerminalEventListener());

        // Get the FragmentManager
        fragmentManager = getSupportFragmentManager();

        // Set up the bottom navigation view
        bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            switch (id) {
                case R.id.action_reader:
                    fragment = new ReaderFragment();
                    break;
                case R.id.action_payment:
                    fragment = new PaymentFragment();
                    break;
            }

            // Switch to the fragment specified
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_container, fragment).commit();
            return true;
        });

        // Default to the reader fragment
        bottomNavigationView.setSelectedItemId(R.id.action_reader);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION && grantResults.length > 0
                && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("Location services are required in order to connect to a " +
                    "reader.");
        }
    }

    /**
     * A simple implementation of the {@link ConnectionTokenProvider} interface. We just request a
     * new token from our backend simulator and forward any exceptions along to the SDK.
     */
    private static class TokenProvider implements ConnectionTokenProvider {

        @Override
        public void fetchConnectionToken(ConnectionTokenCallback callback) {
            try {
                String token = BackendSimulator.createConnectionToken();
                callback.onSuccess(token);
            } catch (StripeException e) {
                callback.onFailure(new ConnectionTokenException(e.getMessage(), e));
            }
        }
    }
}

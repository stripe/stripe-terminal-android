package com.stripe.example

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.stripe.example.fragment.ConnectedReaderFragment
import com.stripe.example.fragment.PaymentFragment
import com.stripe.example.fragment.TerminalFragment
import com.stripe.example.fragment.UpdateReaderFragment
import com.stripe.example.fragment.discovery.DiscoveryFragment
import com.stripe.example.fragment.event.EventFragment
import com.stripe.example.network.ApiClient
import com.stripe.example.network.TokenProvider
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.log.LogLevel
import com.stripe.stripeterminal.model.external.ConnectionStatus
import com.stripe.stripeterminal.model.external.TerminalException

class MainActivity : AppCompatActivity(), NavigationListener {

    companion object {
        // The code that denotes the request for location permissions
        private const val REQUEST_CODE_LOCATION = 1
    }

    /**
     * Upon starting, we should verify we have the permissions we need, then start the app
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Check that the example app has been configured correctly
        if (ApiClient.BACKEND_URL.isEmpty()) {
            throw RuntimeException("You need to set the BACKEND_URL constant in ApiClient.kt " +
                    "before you'll be able to use the example app.")
        }

        if (BluetoothAdapter.getDefaultAdapter()?.isEnabled == false) {
            BluetoothAdapter.getDefaultAdapter().enable()
        }
    }

    override fun onResume() {
        super.onResume()

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!Terminal.isInitialized() && verifyGpsEnabled()) {
                initialize()
            }
        } else {
            // If we don't have them yet, request them before doing anything else
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION)
        }
    }

    /**
     * Receive the result of our permissions check, and initialize if we can
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // If we receive a response to our permission check, initialize
        if (requestCode == REQUEST_CODE_LOCATION && !Terminal.isInitialized() && verifyGpsEnabled()) {
            initialize()
        }
    }

    // Navigation callbacks

    /**
     * Callback function called when discovery has been canceled by the [DiscoveryFragment]
     */
    override fun onCancelDiscovery() {
        navigateTo(TerminalFragment.TAG, TerminalFragment())
    }

    /**
     * Callback function called once discovery has been selected by the [TerminalFragment]
     */
    override fun onRequestDiscovery(isSimulated: Boolean) {
        navigateTo(DiscoveryFragment.TAG, DiscoveryFragment.newInstance(isSimulated))
    }

    /**
     * Callback function called to exit the payment workflow
     */
    override fun onRequestExitWorkflow() {
        if (Terminal.getInstance().connectionStatus == ConnectionStatus.CONNECTED) {
            navigateTo(ConnectedReaderFragment.TAG, ConnectedReaderFragment())
        } else {
            navigateTo(TerminalFragment.TAG, TerminalFragment())
        }
    }

    /**
     * Callback function called to start a payment by the [PaymentFragment]
     */
    override fun onRequestPayment(amount: Int, currency: String) {
        navigateTo(EventFragment.TAG, EventFragment.requestPayment(amount, currency))
    }

    /**
     * Callback function called once the payment workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    override fun onSelectPaymentWorkflow() {
        navigateTo(PaymentFragment.TAG, PaymentFragment())
    }

    /**
     * Callback function called once the read card workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    override fun onSelectReadReusableCardWorkflow() {
        navigateTo(EventFragment.TAG, EventFragment.readReusableCard())
    }

    /**
     * Callback function called once the update reader workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    override fun onSelectUpdateWorkflow() {
        navigateTo(UpdateReaderFragment.TAG, UpdateReaderFragment())
    }

    // Terminal event callbacks

    /**
     * Callback function called when collect payment method has been canceled
     */
    override fun onCancelCollectPaymentMethod() {
        navigateTo(ConnectedReaderFragment.TAG, ConnectedReaderFragment())
    }

    /**
     * Callback function called on completion of [Terminal.connectReader]
     */
    override fun onConnectReader() {
        navigateTo(ConnectedReaderFragment.TAG, ConnectedReaderFragment())
    }

    override fun onDisconnectReader() {
        navigateTo(TerminalFragment.TAG, TerminalFragment())
    }

    /**
     * Initialize the [Terminal] and go to the [TerminalFragment]
     */
    private fun initialize() {
        // Initialize the Terminal as soon as possible
        try {
            Terminal.initTerminal(applicationContext, LogLevel.VERBOSE, TokenProvider(),
                    TerminalEventListener())
        } catch (e: TerminalException) {
            throw RuntimeException("Location services are required in order to initialize " +
                    "the Terminal.", e)
        }

        navigateTo(TerminalFragment.TAG, TerminalFragment())
    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment Fragment to navigate to.
     */
    private fun navigateTo(tag: String, fragment: Fragment) {
        val frag = supportFragmentManager.findFragmentByTag(tag) ?: fragment
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, frag, tag)
                .commitAllowingStateLoss()
    }

    private fun verifyGpsEnabled(): Boolean {
        val locationManager: LocationManager? =
                applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var gpsEnabled = false

        try {
            gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        } catch (exception: Exception) {}

        if (!gpsEnabled) {
            // notify user
            AlertDialog.Builder(ContextThemeWrapper(this, R.style.Theme_MaterialComponents_DayNight_DarkActionBar))
                    .setMessage("Please enable location services")
                    .setCancelable(false)
                    .setPositiveButton("Open location settings") { param, paramInt ->
                        this.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .create()
                    .show()
        }

        return gpsEnabled
    }
}

package com.stripe.example

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.stripe.example.fragment.ConnectedReaderFragment
import com.stripe.example.fragment.PaymentFragment
import com.stripe.example.fragment.TerminalFragment
import com.stripe.example.fragment.UpdateReaderFragment
import com.stripe.example.fragment.discovery.DiscoveryFragment
import com.stripe.example.fragment.discovery.DiscoveryMethod
import com.stripe.example.fragment.event.EventFragment
import com.stripe.example.fragment.location.LocationCreateFragment
import com.stripe.example.fragment.location.LocationSelectionController
import com.stripe.example.fragment.location.LocationSelectionFragment
import com.stripe.example.network.TokenProvider
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.ReaderListener
import com.stripe.stripeterminal.external.models.ConnectionStatus
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage
import com.stripe.stripeterminal.external.models.ReaderInputOptions
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.log.LogLevel

class MainActivity :
    AppCompatActivity(),
    NavigationListener,
    ReaderListener,
    LocationSelectionController {

    /**
     * Upon starting, we should verify we have the permissions we need, then start the app
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            BluetoothAdapter.getDefaultAdapter()?.let { adapter ->
                if (!adapter.isEnabled) {
                    adapter.enable()
                }
            }
        } else {
            Log.w(MainActivity::class.java.simpleName, "Failed to acquire Bluetooth permission")
        }

        initialize()
    }

    // Navigation callbacks

    /**
     * Callback function called when discovery has been canceled by the [DiscoveryFragment]
     */
    override fun onCancelDiscovery() {
        navigateTo(TerminalFragment.TAG, TerminalFragment())
    }

    override fun onRequestChangeLocation() {
        navigateTo(
            LocationSelectionFragment.TAG,
            LocationSelectionFragment.newInstance(),
            replace = false,
            addToBackStack = true,
        )
    }

    override fun onRequestCreateLocation() {
        navigateTo(
            LocationCreateFragment.TAG,
            LocationCreateFragment.newInstance(),
            replace = false,
            addToBackStack = true,
        )
    }

    override fun onLocationCreated() {
        supportFragmentManager.popBackStackImmediate()
        (supportFragmentManager.fragments.last() as? LocationSelectionFragment)?.reload()
    }

    /**
     * Callback function called once discovery has been selected by the [TerminalFragment]
     */
    override fun onRequestDiscovery(isSimulated: Boolean, discoveryMethod: DiscoveryMethod) {
        navigateTo(DiscoveryFragment.TAG, DiscoveryFragment.newInstance(isSimulated, discoveryMethod))
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
    override fun onRequestPayment(
        amount: Long,
        currency: String,
        skipTipping: Boolean,
        extendedAuth: Boolean,
        incrementalAuth: Boolean
    ) {
        navigateTo(
            EventFragment.TAG, EventFragment.requestPayment(amount, currency, skipTipping, extendedAuth, incrementalAuth)
        )
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
    override fun onRequestSaveCard() {
        navigateTo(EventFragment.TAG, EventFragment.collectSetupIntentPaymentMethod())
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
     * Callback function called when collect setup intent has been canceled
     */
    override fun onCancelCollectSetupIntent() {
        navigateTo(ConnectedReaderFragment.TAG, ConnectedReaderFragment())
    }

    /**
     * Callback function called on completion of [Terminal.connectBluetoothReader]
     */
    override fun onConnectReader() {
        navigateTo(ConnectedReaderFragment.TAG, ConnectedReaderFragment())
    }

    override fun onDisconnectReader() {
        navigateTo(TerminalFragment.TAG, TerminalFragment())
    }

    override fun onStartInstallingUpdate(update: ReaderSoftwareUpdate, cancelable: Cancelable?) {
        runOnUiThread {
            // Delegate out to the current fragment, if it acts as a ReaderListener
            supportFragmentManager.fragments.last()?.let {
                if (it is ReaderListener) {
                    it.onStartInstallingUpdate(update, cancelable)
                }
            }
        }
    }

    override fun onReportReaderSoftwareUpdateProgress(progress: Float) {
        runOnUiThread {
            // Delegate out to the current fragment, if it acts as a ReaderListener
            supportFragmentManager.fragments.last()?.let {
                if (it is ReaderListener) {
                    it.onReportReaderSoftwareUpdateProgress(progress)
                }
            }
        }
    }

    override fun onFinishInstallingUpdate(update: ReaderSoftwareUpdate?, e: TerminalException?) {
        runOnUiThread {
            // Delegate out to the current fragment, if it acts as a ReaderListener
            supportFragmentManager.fragments.last()?.let {
                if (it is ReaderListener) {
                    it.onFinishInstallingUpdate(update, e)
                }
            }
        }
    }

    override fun onRequestReaderInput(options: ReaderInputOptions) {
        runOnUiThread {
            // Delegate out to the current fragment, if it acts as a ReaderListener
            supportFragmentManager.fragments.last()?.let {
                if (it is ReaderListener) {
                    it.onRequestReaderInput(options)
                }
            }
        }
    }

    override fun onRequestReaderDisplayMessage(message: ReaderDisplayMessage) {
        runOnUiThread {
            // Delegate out to the current fragment, if it acts as a ReaderListener
            supportFragmentManager.fragments.last()?.let {
                if (it is ReaderListener) {
                    it.onRequestReaderDisplayMessage(message)
                }
            }
        }
    }

    override fun onLocationSelected(location: Location) {
        supportFragmentManager.popBackStackImmediate()
        (supportFragmentManager.fragments.last() as? LocationSelectionController)?.onLocationSelected(location)
    }

    override fun onLocationCleared() {
        supportFragmentManager.popBackStackImmediate()
        (supportFragmentManager.fragments.last() as? LocationSelectionController)?.onLocationCleared()
    }

    /**
     * Initialize the [Terminal] and go to the [TerminalFragment]
     */
    private fun initialize() {
        // Initialize the Terminal as soon as possible
        try {
            if (!Terminal.isInitialized()) {
                Terminal.initTerminal(applicationContext, LogLevel.VERBOSE, TokenProvider(), TerminalEventListener())
            }
        } catch (e: TerminalException) {
            throw RuntimeException(e)
        }

        navigateTo(TerminalFragment.TAG, TerminalFragment())
    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment Fragment to navigate to.
     */
    private fun navigateTo(
        tag: String,
        fragment: Fragment,
        replace: Boolean = true,
        addToBackStack: Boolean = false,
    ) {
        val frag = supportFragmentManager.findFragmentByTag(tag) ?: fragment
        supportFragmentManager
            .beginTransaction()
            .apply {
                if (replace) {
                    replace(R.id.container, frag, tag)
                } else {
                    add(R.id.container, frag, tag)
                }

                if (addToBackStack) {
                    addToBackStack(tag)
                }
            }
            .commitAllowingStateLoss()
    }
}

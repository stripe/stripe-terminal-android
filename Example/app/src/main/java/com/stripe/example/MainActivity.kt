package com.stripe.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import com.stripe.stripeterminal.*
import kotlinx.android.synthetic.main.log_event_layout.view.*

class MainActivity : AppCompatActivity(), NavigationListener, TerminalStateManager,
        ReaderInputListener {

    companion object {

        // The code that denotes the request for location permissions
        private const val REQUEST_CODE_LOCATION = 1;
    }

    private var collectCancelable: Cancelable? = null
    private var discoveryCancelable: Cancelable? = null
    private var simulated: Boolean = false

    /**
     * Upon starting, we should verify we have the permissions we need, then start the app
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initialize()
        } else {
            // If we don't have them yet, request them before doing anything else
            val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION)
        }
    }

    /**
     * Receive the result of our permissions check, and initialize if we can
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        // If we receive a response to our permission check, initialize
        if (requestCode == REQUEST_CODE_LOCATION) {
            initialize()
        }
    }

    // Navigation callbacks

    /**
     * Callback function called when there's been a request to cancel collect payment method
     */
    override fun onRequestCancelCollectPaymentMethod() {
        collectCancelable?.cancel(CollectPaymentMethodCancellationCallback(this))
    }

    /**
     * Callback function called when there's been a request to cancel discovery
     */
    override fun onRequestCancelDiscovery() {
        discoveryCancelable?.cancel(DiscoveryCancellationCallback(this))
    }

    /**
     * Callback function called once disconnect has been selected by the [ConnectedReaderFragment]
     */
    override fun onRequestDisconnect() {
        Terminal.getInstance().disconnectReader(DisconnectCallback(this))
    }

    /**
     * Callback function called once discovery has been selected by the [TerminalFragment]
     */
    override fun onRequestDiscovery() {
        val fragment = DiscoveryFragment()
        navigateTo(fragment)
        discoveryCancelable = Terminal.getInstance().discoverReaders(DiscoveryConfiguration(0,
                DeviceType.CHIPPER_2X, simulated), fragment, DiscoveryCallback(this))
    }

    /**
     * Callback function called to exit the payment workflow
     */
    override fun onRequestExitPaymentWorkflow() {
        navigateTo(ConnectedReaderFragment())
    }

    /**
     * Callback function called to start a payment by the [PaymentFragment]
     */
    override fun onRequestPayment(amount: Int, currency: String) {
        val params = PaymentIntentParameters.Builder()
                .setAmount(amount)
                .setCurrency(currency.toLowerCase())
                .build()
        navigateTo(EventFragment())
        Terminal.getInstance().createPaymentIntent(params, CreatePaymentIntentCallback(this))
    }

    /**
     * Callback function called once the payment workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    override fun onSelectPaymentWorkflow() {
        navigateTo(PaymentFragment())
    }

    /**
     * Callback function called once a [Reader] has been selected by the [DiscoveryFragment]
     */
    override fun onSelectReader(reader: Reader) {
        navigateTo(ConnectingFragment())
        Terminal.getInstance().connectReader(reader, ConnectionCallback(this))
    }

    /**
     * Callback function called when the simulated switch has been toggled in the [TerminalFragment]
     */
    override fun onToggleSimulatedSwitch(isOn: Boolean) {
        simulated = isOn
    }

    // Terminal event callbacks

    /**
     * Callback function called when collect payment method has been canceled
     */
    override fun onCancelCollectPaymentMethod() {
        collectCancelable = null
        navigateTo(ConnectedReaderFragment())
    }

    /**
     * Callback function called when discovery has been canceled
     */
    override fun onCancelDiscovery() {
        discoveryCancelable = null
        navigateTo(TerminalFragment().setSimulatedSwitch(simulated))
    }

    /**
     * Callback function called on completion of [Terminal.collectPaymentMethod]
     */
    override fun onCollectPaymentMethod(paymentIntent: PaymentIntent) {
        displayEvent("Collected PaymentMethod", "terminal.collectPaymentMethod")
        Terminal.getInstance().confirmPaymentIntent(paymentIntent, ConfirmPaymentIntentCallback(this))
        collectCancelable = null
    }

    /**
     * Callback function called on completion of [Terminal.confirmPaymentIntent]
     */
    override fun onConfirmPaymentIntent(paymentIntent: PaymentIntent) {
        displayEvent("Confirmed PaymentIntent", "terminal.confirmPaymentIntent")
        BackendSimulator.capturePaymentIntent(paymentIntent.id)
        displayEvent("Captured PaymentIntent", "backend.capturePaymentIntent")

        // Tell the EventFragment that the flow has completed
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment is EventFragment) {
            runOnUiThread {
                fragment.completeFlow()
            }
        }
    }

    /**
     * Callback function called on completion of [Terminal.connectReader]
     */
    override fun onConnectReader() {
        navigateTo(ConnectedReaderFragment())
    }

    /**
     * Callback function called on completion of [Terminal.createPaymentIntent]
     */
    override fun onCreatePaymentIntent(paymentIntent: PaymentIntent) {
        displayEvent("Created PaymentIntent", "terminal.createPaymentIntent")
        collectCancelable = Terminal.getInstance().collectPaymentMethod(paymentIntent, this,
                CollectPaymentMethodCallback(this))
    }

    /**
     * Callback function called on completion of [Terminal.disconnectReader]
     */
    override fun onDisconnectReader() {
        navigateTo(TerminalFragment().setSimulatedSwitch(simulated))
    }

    /**
     * Callback function called on completion of [Terminal.discoverReaders]
     */
    override fun onDiscoverReaders() {
        discoveryCancelable = null
    }

    /**
     * Callback function called whenever a [Terminal] method fails
     */
    override fun onFailure(e: TerminalException) {
        displayEvent(e.errorMessage, e.errorCode.toString())
    }

    // Reader prompt callbacks

    /**
     * Callback function called when the [Reader] is ready for input
     */
    override fun onBeginWaitingForReaderInput(options: ReaderInputOptions) {
        displayEvent(options.toString(), "listener.onBeginWaitingForReaderInput")
    }

    /**
     * Callback function called when the [Reader] needs to display a prompt
     */
    override fun onRequestReaderInputPrompt(prompt: ReaderInputPrompt) {
        displayEvent(prompt.toString(), "listener.onRequestReaderInputPrompt")
    }

    /**
     * Display [Terminal] events in the EventFragment if it's visible
     */
    private fun displayEvent(message: String, method: String) {
        runOnUiThread {
            val fragment = supportFragmentManager.findFragmentById(R.id.container)
            if (fragment is EventFragment) {
                fragment.displayEvent(message, method)
            }
        }
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

        navigateTo(TerminalFragment().setSimulatedSwitch(simulated))
    }


    /**
     * Navigate to the given fragment.
     *
     * @param fragment Fragment to navigate to.
     */
    fun navigateTo(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

}

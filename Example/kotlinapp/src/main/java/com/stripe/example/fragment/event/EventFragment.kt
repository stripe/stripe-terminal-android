package com.stripe.example.fragment.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.FragmentEventBinding
import com.stripe.example.fragment.TerminalFragment
import com.stripe.example.model.Event
import com.stripe.example.model.PaymentIntentCreationResponse
import com.stripe.example.network.ApiClient
import com.stripe.example.viewmodel.EventViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.OnReaderTips
import com.stripe.stripeterminal.external.callable.BluetoothReaderListener
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.callable.PaymentMethodCallback
import com.stripe.stripeterminal.external.models.CardPresentParameters
import com.stripe.stripeterminal.external.models.CollectConfiguration
import com.stripe.stripeterminal.external.models.DiscoveryMethod
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentParameters
import com.stripe.stripeterminal.external.models.PaymentMethod
import com.stripe.stripeterminal.external.models.PaymentMethodOptionsParameters
import com.stripe.stripeterminal.external.models.ReadReusableCardParameters
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage
import com.stripe.stripeterminal.external.models.ReaderInputOptions
import com.stripe.stripeterminal.external.models.TerminalException
import retrofit2.Call
import retrofit2.Response
import java.lang.ref.WeakReference
import java.util.Locale

/**
 * The `EventFragment` displays events as they happen during a payment flow
 */
class EventFragment : Fragment(), BluetoothReaderListener {

    companion object {
        const val TAG = "com.stripe.example.fragment.event.EventFragment"

        private const val AMOUNT = "com.stripe.example.fragment.event.EventFragment.amount"
        private const val CURRENCY = "com.stripe.example.fragment.event.EventFragment.currency"
        private const val REQUEST_PAYMENT = "com.stripe.example.fragment.event.EventFragment.request_payment"
        private const val READ_REUSABLE_CARD = "com.stripe.example.fragment.event.EventFragment.read_reusable_card"
        private const val SKIP_TIPPING = "com.stripe.example.fragment.event.EventFragment.skip_tipping"
        private const val EXTENDED_AUTH = "com.stripe.example.fragment.event.EventFragment.extended_auth"
        private const val INCREMENTAL_AUTH = "com.stripe.example.fragment.event.EventFragment.incremental_auth"

        fun readReusableCard(): EventFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putBoolean(READ_REUSABLE_CARD, true)
            bundle.putBoolean(REQUEST_PAYMENT, false)
            fragment.arguments = bundle
            return fragment
        }

        fun requestPayment(
            amount: Long,
            currency: String,
            skipTipping: Boolean,
            extendedAuth: Boolean,
            incrementalAuth: Boolean
        ): EventFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putLong(AMOUNT, amount)
            bundle.putString(CURRENCY, currency)
            bundle.putBoolean(REQUEST_PAYMENT, true)
            bundle.putBoolean(READ_REUSABLE_CARD, false)
            bundle.putBoolean(SKIP_TIPPING, skipTipping)
            bundle.putBoolean(EXTENDED_AUTH, extendedAuth)
            bundle.putBoolean(INCREMENTAL_AUTH, incrementalAuth)

            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var adapter: EventAdapter
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var activityRef: WeakReference<FragmentActivity?>

    private lateinit var binding: FragmentEventBinding
    private lateinit var viewModel: EventViewModel

    private var paymentIntent: PaymentIntent? = null

    private val processPaymentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                addEvent("Processed payment", "terminal.processPayment")
                ApiClient.capturePaymentIntent(paymentIntent.id)
                addEvent("Captured PaymentIntent", "backend.capturePaymentIntent")
                completeFlow()
            }

            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    private val cancelPaymentIntentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                addEvent("Canceled PaymentIntent", "terminal.cancelPaymentIntent")
                activityRef.get()?.let {
                    if (it is NavigationListener) {
                        it.runOnUiThread {
                            it.onCancelCollectPaymentMethod()
                        }
                    }
                }
            }

            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    private val collectPaymentMethodCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                addEvent("Collected PaymentMethod", "terminal.collectPaymentMethod")
                Terminal.getInstance().processPayment(paymentIntent, processPaymentCallback)
                viewModel.collectTask = null
            }

            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    @OptIn(OnReaderTips::class)
    private val createPaymentIntentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                val skipTipping = arguments?.getBoolean(SKIP_TIPPING) ?: false
                val collectConfig = CollectConfiguration(skipTipping = skipTipping)
                this@EventFragment.paymentIntent = paymentIntent
                addEvent("Created PaymentIntent", "terminal.createPaymentIntent")
                viewModel.collectTask = Terminal.getInstance().collectPaymentMethod(
                    paymentIntent, collectPaymentMethodCallback, collectConfig
                )
            }

            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    private val reusablePaymentMethodCallback by lazy {
        object : PaymentMethodCallback {
            override fun onSuccess(paymentMethod: PaymentMethod) {
                addEvent("Created PaymentMethod: ${paymentMethod.id}", "terminal.readReusableCard")
                completeFlow()
            }

            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRef = WeakReference(activity)
        viewModel = ViewModelProvider(this)[EventViewModel::class.java]
        adapter = EventAdapter(viewModel)

        if (savedInstanceState == null) {
            arguments?.let {
                if (it.getBoolean(REQUEST_PAYMENT)) {
                    val extendedAuth = it.getBoolean(EXTENDED_AUTH)
                    val incrementalAuth = it.getBoolean(INCREMENTAL_AUTH)
                    // For internet-connected readers, PaymentIntents must be created via your backend
                    if (TerminalFragment.getCurrentDiscoveryMethod(activityRef.get()) == DiscoveryMethod.INTERNET) {
                        ApiClient.createPaymentIntent(
                            it.getLong(AMOUNT),
                            it.getString(CURRENCY)?.lowercase(Locale.ENGLISH) ?: "usd",
                            extendedAuth,
                            incrementalAuth,
                            object : retrofit2.Callback<PaymentIntentCreationResponse> {
                                override fun onResponse(
                                    call: Call<PaymentIntentCreationResponse>,
                                    response: Response<PaymentIntentCreationResponse>
                                ) {
                                    if (response.isSuccessful && response.body() != null)
                                        Terminal.getInstance().retrievePaymentIntent(
                                            response.body()?.secret!!,
                                            createPaymentIntentCallback
                                        )
                                    else {
                                        Toast.makeText(
                                            activity,
                                            "PaymentIntent creation failed",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }

                                override fun onFailure(
                                    call: Call<PaymentIntentCreationResponse>,
                                    t: Throwable
                                ) {
                                    Toast.makeText(
                                        activity,
                                        "PaymentIntent creation failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        )
                    } else {
                        val cardPresentParametersBuilder = CardPresentParameters.Builder()
                        if (extendedAuth) {
                            cardPresentParametersBuilder.setRequestExtendedAuthorization(true)
                        }
                        if (incrementalAuth) {
                            cardPresentParametersBuilder.setRequestIncrementalAuthorizationSupport(true)
                        }

                        val paymentMethodOptionsParameters = PaymentMethodOptionsParameters.Builder()
                            .setCardPresentParameters(cardPresentParametersBuilder.build())
                            .build()

                        val params = PaymentIntentParameters.Builder()
                            .setAmount(it.getLong(AMOUNT))
                            .setCurrency(it.getString(CURRENCY)?.lowercase(Locale.ENGLISH) ?: "usd")
                            .setPaymentMethodOptionsParameters(paymentMethodOptionsParameters)
                            .build()
                        Terminal.getInstance()
                            .createPaymentIntent(params, createPaymentIntentCallback)
                    }
                } else if (it.getBoolean(READ_REUSABLE_CARD)) {
                    viewModel.collectTask = Terminal.getInstance().readReusableCard(
                        ReadReusableCardParameters.NULL, reusablePaymentMethodCallback
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        eventRecyclerView = binding.eventRecyclerView
        eventRecyclerView.layoutManager = LinearLayoutManager(activity)
        eventRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.cancelButton.setOnClickListener {
            viewModel.collectTask?.cancel(object : Callback {
                override fun onSuccess() {
                    viewModel.collectTask = null
                    paymentIntent?.let {
                        Terminal.getInstance().cancelPaymentIntent(it, cancelPaymentIntentCallback)
                    }
                }

                override fun onFailure(e: TerminalException) {
                    viewModel.collectTask = null
                    this@EventFragment.onFailure(e)
                }
            })
        }

        binding.doneButton.setOnClickListener {
            activityRef.get()?.let {
                if (it is NavigationListener) {
                    it.runOnUiThread {
                        it.onRequestExitWorkflow()
                    }
                }
            }
        }
    }

    override fun onRequestReaderDisplayMessage(message: ReaderDisplayMessage) {
        addEvent(message.toString(), "listener.onRequestReaderDisplayMessage")
    }

    override fun onRequestReaderInput(options: ReaderInputOptions) {
        addEvent(options.toString(), "listener.onRequestReaderInput")
    }

    fun completeFlow() {
        activityRef.get()?.let {
            it.runOnUiThread {
                viewModel.isComplete.value = true
            }
        }
    }

    fun addEvent(message: String, method: String) {
        activityRef.get()?.let { activity ->
            activity.runOnUiThread {
                viewModel.addEvent(Event(message, method))
            }
        }
    }

    private fun onFailure(e: TerminalException) {
        addEvent(e.errorMessage, e.errorCode.toString())
        completeFlow()
    }
}

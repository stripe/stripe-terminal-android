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
import com.stripe.example.fragment.discovery.DiscoveryMethod
import com.stripe.example.model.Event
import com.stripe.example.model.OfflineBehaviorSelection
import com.stripe.example.network.ApiClient
import com.stripe.example.viewmodel.EventViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.OfflineMode
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.MobileReaderListener
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.callable.SetupIntentCallback
import com.stripe.stripeterminal.external.models.AllowRedisplay
import com.stripe.stripeterminal.external.models.CardPresentParameters
import com.stripe.stripeterminal.external.models.CollectConfiguration
import com.stripe.stripeterminal.external.models.CreateConfiguration
import com.stripe.stripeterminal.external.models.DisconnectReason
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentParameters
import com.stripe.stripeterminal.external.models.PaymentMethodOptionsParameters
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage
import com.stripe.stripeterminal.external.models.ReaderInputOptions
import com.stripe.stripeterminal.external.models.SetupIntent
import com.stripe.stripeterminal.external.models.SetupIntentCancellationParameters
import com.stripe.stripeterminal.external.models.SetupIntentParameters
import com.stripe.stripeterminal.external.models.TerminalException
import retrofit2.Call
import retrofit2.Response
import java.lang.ref.WeakReference
import java.util.Locale

/**
 * The `EventFragment` displays events as they happen during a payment flow
 */
@OptIn(OfflineMode::class)
class EventFragment : Fragment(), MobileReaderListener {

    companion object {
        const val TAG = "com.stripe.example.fragment.event.EventFragment"

        private const val AMOUNT = "com.stripe.example.fragment.event.EventFragment.amount"
        private const val CURRENCY = "com.stripe.example.fragment.event.EventFragment.currency"
        private const val REQUEST_PAYMENT = "com.stripe.example.fragment.event.EventFragment.request_payment"
        private const val SAVE_CARD = "com.stripe.example.fragment.event.EventFragment.save_card"
        private const val SKIP_TIPPING = "com.stripe.example.fragment.event.EventFragment.skip_tipping"
        private const val EXTENDED_AUTH = "com.stripe.example.fragment.event.EventFragment.extended_auth"
        private const val INCREMENTAL_AUTH = "com.stripe.example.fragment.event.EventFragment.incremental_auth"
        private const val OFFLINE_BEHAVIOR = "com.stripe.example.fragment.event.EventFragment.offline_behavior"

        fun collectSetupIntentPaymentMethod(): EventFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putBoolean(SAVE_CARD, true)
            bundle.putBoolean(REQUEST_PAYMENT, false)
            fragment.arguments = bundle
            return fragment
        }

        fun requestPayment(
            amount: Long,
            currency: String,
            skipTipping: Boolean,
            extendedAuth: Boolean,
            incrementalAuth: Boolean,
            offlineBehaviorSelection: OfflineBehaviorSelection
        ): EventFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putLong(AMOUNT, amount)
            bundle.putString(CURRENCY, currency)
            bundle.putBoolean(REQUEST_PAYMENT, true)
            bundle.putBoolean(SAVE_CARD, false)
            bundle.putBoolean(SKIP_TIPPING, skipTipping)
            bundle.putBoolean(EXTENDED_AUTH, extendedAuth)
            bundle.putBoolean(INCREMENTAL_AUTH, incrementalAuth)
            bundle.putSerializable(OFFLINE_BEHAVIOR, offlineBehaviorSelection)
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
    private var setupIntent: SetupIntent? = null

    private val confirmPaymentIntentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                addEvent("Confirmed payment", "terminal.confirmPaymentIntent")
                paymentIntent.id?.let {
                    ApiClient.capturePaymentIntent(it)
                    addEvent("Captured PaymentIntent", "backend.capturePaymentIntent")
                }
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
                Terminal.getInstance().confirmPaymentIntent(paymentIntent, confirmPaymentIntentCallback)
                viewModel.collectTask = null
            }

            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    private val createPaymentIntentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                val skipTipping = arguments?.getBoolean(SKIP_TIPPING) ?: false
                val collectConfig = CollectConfiguration.Builder()
                    .skipTipping(skipTipping)
                    .build()
                this@EventFragment.paymentIntent = paymentIntent
                addEvent("Created PaymentIntent", "terminal.createPaymentIntent")
                viewModel.collectTask = Terminal.getInstance().collectPaymentMethod(
                    paymentIntent,
                    collectPaymentMethodCallback,
                    collectConfig
                )
            }

            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    private val createSetupIntentCallback: SetupIntentCallback = object : SetupIntentCallback {
        override fun onSuccess(setupIntent: SetupIntent) {
            this@EventFragment.setupIntent = setupIntent
            addEvent("Created SetupIntent", "terminal.createSetupIntent")
            viewModel.collectTask = Terminal.getInstance().collectSetupIntentPaymentMethod(
                setupIntent,
                allowRedisplay = AllowRedisplay.ALWAYS,
                callback = collectSetupIntentPaymentMethodCallback,
            )
        }

        override fun onFailure(e: TerminalException) {
            this@EventFragment.onFailure(e)
        }
    }

    private val collectSetupIntentPaymentMethodCallback: SetupIntentCallback = object : SetupIntentCallback {
        override fun onSuccess(setupIntent: SetupIntent) {
            addEvent("Collected PaymentMethod", "terminal.collectSetupIntentPaymentMethod")
            viewModel.collectTask = null
            completeFlow()
        }

        override fun onFailure(e: TerminalException) {
            this@EventFragment.onFailure(e)
        }
    }

    private val cancelSetupIntentCallback by lazy {
        object : SetupIntentCallback {
            override fun onSuccess(setupIntent: SetupIntent) {
                addEvent("Canceled SetupIntent", "terminal.cancelSetupIntent")
                activityRef.get()?.let {
                    if (it is NavigationListener) {
                        it.runOnUiThread {
                            it.onCancelCollectSetupIntent()
                        }
                    }
                }
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
                    val cardPresentParametersBuilder = CardPresentParameters.Builder()
                    if (extendedAuth) {
                        cardPresentParametersBuilder.setRequestExtendedAuthorization(true)
                    }
                    if (incrementalAuth) {
                        cardPresentParametersBuilder.setRequestIncrementalAuthorizationSupport(true)
                    }
                    val offlineBehavior =
                            (it.getSerializable(OFFLINE_BEHAVIOR) as? OfflineBehaviorSelection)?.offlineBehavior

                    val paymentMethodOptionsParameters = PaymentMethodOptionsParameters.Builder()
                            .setCardPresentParameters(cardPresentParametersBuilder.build())
                            .build()

                    val params = PaymentIntentParameters.Builder()
                            .setAmount(it.getLong(AMOUNT))
                            .setCurrency(it.getString(CURRENCY)?.lowercase(Locale.ENGLISH) ?: "usd")
                            .setPaymentMethodOptionsParameters(paymentMethodOptionsParameters)
                            .build()
                    val createConfiguration = offlineBehavior?.let(::CreateConfiguration)
                    Terminal.getInstance()
                        .createPaymentIntent(params, createPaymentIntentCallback, createConfiguration)
                } else if (it.getBoolean(SAVE_CARD)) {
                    val params: SetupIntentParameters = SetupIntentParameters.Builder().build()
                    Terminal.getInstance().createSetupIntent(params, createSetupIntentCallback)
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
                        if (TerminalFragment.getCurrentDiscoveryMethod(activityRef.get()) == DiscoveryMethod.INTERNET) {
                            it.id?.let { intentId ->
                                ApiClient.cancelPaymentIntent(
                                    intentId,
                                    object : retrofit2.Callback<Void> {
                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                            if (response.isSuccessful) {
                                                addEvent("Canceled PaymentIntent", "backend.cancelPaymentIntent")
                                                activityRef.get()?.let { activity ->
                                                    if (activity is NavigationListener) {
                                                        activity.runOnUiThread {
                                                            activity.onCancelCollectPaymentMethod()
                                                        }
                                                    }
                                                }
                                            } else {
                                                addEvent("Cancel PaymentIntent failed", "backend.cancelPaymentIntent")
                                                completeFlow()
                                            }
                                        }

                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                            Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
                                            completeFlow()
                                        }
                                    }
                                )
                            } ?: run {
                                addEvent("Cancel PaymentIntent Skipped", "backend.cancelPaymentIntent")
                                completeFlow()
                            }
                        } else {
                            Terminal.getInstance().cancelPaymentIntent(it, cancelPaymentIntentCallback)
                        }
                    }
                    setupIntent?.let {
                        val params = SetupIntentCancellationParameters.NULL
                        Terminal.getInstance().cancelSetupIntent(it, params, cancelSetupIntentCallback)
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

    override fun onDisconnect(reason: DisconnectReason) {
        addEvent(reason.name, "listener.onDisconnect")
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

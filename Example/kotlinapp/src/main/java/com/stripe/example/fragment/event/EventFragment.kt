package com.stripe.example.fragment.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.FragmentEventBinding
import com.stripe.example.model.Event
import com.stripe.example.network.ApiClient
import com.stripe.example.viewmodel.EventViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.callable.Callback
import com.stripe.stripeterminal.callable.PaymentIntentCallback
import com.stripe.stripeterminal.callable.PaymentMethodCallback
import com.stripe.stripeterminal.callable.ReaderDisplayListener
import com.stripe.stripeterminal.model.external.PaymentIntent
import com.stripe.stripeterminal.model.external.PaymentIntentParameters
import com.stripe.stripeterminal.model.external.PaymentMethod
import com.stripe.stripeterminal.model.external.ReadReusableCardParameters
import com.stripe.stripeterminal.model.external.ReaderDisplayMessage
import com.stripe.stripeterminal.model.external.ReaderInputOptions
import com.stripe.stripeterminal.model.external.TerminalException
import java.lang.ref.WeakReference
import java.util.Locale
import kotlinx.android.synthetic.main.fragment_event.cancel_button
import kotlinx.android.synthetic.main.fragment_event.done_button
import kotlinx.android.synthetic.main.fragment_event.view.event_recycler_view

/**
 * The `EventFragment` displays events as they happen during a payment flow
 */
class EventFragment : Fragment(), ReaderDisplayListener {

    companion object {
        const val TAG = "com.stripe.example.fragment.event.EventFragment"

        private const val AMOUNT = "com.stripe.example.fragment.event.EventFragment.amount"
        private const val CURRENCY = "com.stripe.example.fragment.event.EventFragment.currency"
        private const val REQUEST_PAYMENT = "com.stripe.example.fragment.event.EventFragment.request_payment"
        private const val READ_REUSABLE_CARD = "com.stripe.example.fragment.event.EventFragment.read_reusable_card"

        fun readReusableCard(): EventFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putBoolean(READ_REUSABLE_CARD, true)
            bundle.putBoolean(REQUEST_PAYMENT, false)
            fragment.arguments = bundle
            return fragment
        }

        fun requestPayment(amount: Int, currency: String): EventFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putInt(AMOUNT, amount)
            bundle.putString(CURRENCY, currency)
            bundle.putBoolean(REQUEST_PAYMENT, true)
            bundle.putBoolean(READ_REUSABLE_CARD, false)
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

    private val createPaymentIntentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                this@EventFragment.paymentIntent = paymentIntent
                addEvent("Created PaymentIntent", "terminal.createPaymentIntent")
                viewModel.collectTask = Terminal.getInstance().collectPaymentMethod(
                        paymentIntent, this@EventFragment, collectPaymentMethodCallback)
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
        viewModel = ViewModelProviders.of(this).get(EventViewModel::class.java)
        adapter = EventAdapter(viewModel)

        if (savedInstanceState == null) {
            arguments?.let {
                if (it.getBoolean(REQUEST_PAYMENT)) {
                    val params = PaymentIntentParameters.Builder()
                            .setAmount(it.getInt(AMOUNT))
                            .setCurrency(it.getString(CURRENCY)?.toLowerCase(Locale.ENGLISH) ?: "usd")
                            .build()
                    Terminal.getInstance().createPaymentIntent(params, createPaymentIntentCallback)
                } else if (it.getBoolean(READ_REUSABLE_CARD)) {
                    viewModel.collectTask = Terminal.getInstance().readReusableCard(
                            ReadReusableCardParameters.NULL, this@EventFragment, reusablePaymentMethodCallback)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        eventRecyclerView = binding.root.event_recycler_view
        eventRecyclerView.layoutManager = LinearLayoutManager(activity)
        eventRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cancel_button.setOnClickListener {
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

        done_button.setOnClickListener {
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

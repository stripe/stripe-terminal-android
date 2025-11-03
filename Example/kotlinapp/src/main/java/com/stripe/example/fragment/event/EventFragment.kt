package com.stripe.example.fragment.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.TerminalRepository
import com.stripe.example.databinding.FragmentEventBinding
import com.stripe.example.model.Event
import com.stripe.example.model.OfflineBehaviorSelection
import com.stripe.example.viewmodel.EventViewModel
import com.stripe.stripeterminal.external.callable.MobileReaderListener
import com.stripe.stripeterminal.external.models.AllowRedisplay
import com.stripe.stripeterminal.external.models.CardPresentParameters
import com.stripe.stripeterminal.external.models.CollectPaymentIntentConfiguration
import com.stripe.stripeterminal.external.models.CollectSetupIntentConfiguration
import com.stripe.stripeterminal.external.models.ConfirmPaymentIntentConfiguration
import com.stripe.stripeterminal.external.models.CreateConfiguration
import com.stripe.stripeterminal.external.models.DisconnectReason
import com.stripe.stripeterminal.external.models.PaymentIntentParameters
import com.stripe.stripeterminal.external.models.PaymentMethodOptionsParameters
import com.stripe.stripeterminal.external.models.PaymentMethodType
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage
import com.stripe.stripeterminal.external.models.ReaderInputOptions
import com.stripe.stripeterminal.external.models.SetupIntentParameters
import java.lang.ref.WeakReference
import java.util.Locale

/**
 * The `EventFragment` displays events as they happen during a payment flow
 */
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
        private const val TRANSACTION_ID = "com.stripe.example.fragment.event.EventFragment.transaction_id"
        private const val REFUND_PAYMENT = "com.stripe.example.fragment.event.EventFragment.refund_payment"
        private const val CANCEL_TRANSACTION = "com.stripe.example.fragment.event.EventFragment.cancel_transaction"

        fun collectSetupIntentPaymentMethod(): EventFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putBoolean(SAVE_CARD, true)
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
            bundle.putBoolean(SKIP_TIPPING, skipTipping)
            bundle.putBoolean(EXTENDED_AUTH, extendedAuth)
            bundle.putBoolean(INCREMENTAL_AUTH, incrementalAuth)
            bundle.putSerializable(OFFLINE_BEHAVIOR, offlineBehaviorSelection)
            fragment.arguments = bundle
            return fragment
        }

        fun refundPayment(transactionId: String): EventFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putBoolean(REFUND_PAYMENT, true)
            bundle.putString(TRANSACTION_ID, transactionId)
            fragment.arguments = bundle
            return fragment
        }

        fun cancelTransaction(transactionId: String): EventFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putBoolean(CANCEL_TRANSACTION, true)
            bundle.putString(TRANSACTION_ID, transactionId)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var adapter: EventAdapter
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var activityRef: WeakReference<FragmentActivity?>

    private lateinit var binding: FragmentEventBinding
    private lateinit var viewModel: EventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRef = WeakReference(activity)
        viewModel = ViewModelProvider(this)[EventViewModel::class.java]
        adapter = EventAdapter(viewModel)

        if (savedInstanceState == null) {
            val fragmentArgs = requireArguments()
            when {
                fragmentArgs.getBoolean(REQUEST_PAYMENT, false) -> handleRequestPayment(fragmentArgs)
                fragmentArgs.getBoolean(SAVE_CARD, false) -> handleSaveCard()
                fragmentArgs.getBoolean(REFUND_PAYMENT, false) -> handleRefundPayment(fragmentArgs)
                fragmentArgs.getBoolean(CANCEL_TRANSACTION, false) -> handleCancelTransaction(fragmentArgs)
            }
        }
    }

    private fun handleRequestPayment(args: Bundle) {
        val extendedAuth = args.getBoolean(EXTENDED_AUTH)
        val incrementalAuth = args.getBoolean(INCREMENTAL_AUTH)
        val currency = args.getString(CURRENCY)?.lowercase(Locale.ENGLISH) ?: "usd"
        val cardPresentParametersBuilder = CardPresentParameters.Builder()
        if (extendedAuth) {
            cardPresentParametersBuilder.setRequestExtendedAuthorization(true)
        }
        if (incrementalAuth) {
            cardPresentParametersBuilder.setRequestIncrementalAuthorizationSupport(true)
        }
        val offlineBehavior = BundleCompat.getSerializable(
            args,
            OFFLINE_BEHAVIOR,
            OfflineBehaviorSelection::class.java
        )?.offlineBehavior

        val paymentMethodOptionsParameters = PaymentMethodOptionsParameters.Builder()
            .setCardPresentParameters(cardPresentParametersBuilder.build())
            .build()

        val params = PaymentIntentParameters.Builder(
            paymentMethodTypes = buildList {
                add(PaymentMethodType.CARD_PRESENT)
                if (currency == "cad") {
                    // Interac is only supported in Canada
                    add(PaymentMethodType.INTERAC_PRESENT)
                }
            }
        )
            .setAmount(args.getLong(AMOUNT))
            .setCurrency(currency)
            .setPaymentMethodOptionsParameters(paymentMethodOptionsParameters)
            .setMetadata(TerminalRepository.genMetaData())
            .build()
        val createConfiguration = offlineBehavior?.let(::CreateConfiguration)
        val skipTipping = arguments?.getBoolean(SKIP_TIPPING) ?: false
        val collectConfig = CollectPaymentIntentConfiguration.Builder()
            .skipTipping(skipTipping)
            .build()

        viewModel.takePayment(
            paymentParameters = params,
            createConfiguration = createConfiguration,
            collectConfiguration = collectConfig,
            confirmConfiguration = ConfirmPaymentIntentConfiguration.Builder().build()
        )
    }

    private fun handleSaveCard() {
        val params = SetupIntentParameters.Builder().setMetadata(TerminalRepository.genMetaData()).build()
        viewModel.saveCard(
            setupIntentParameters = params,
            allowRedisplay = AllowRedisplay.ALWAYS,
            collectConfiguration = CollectSetupIntentConfiguration.Builder().build()
        )
    }

    private fun handleRefundPayment(args: Bundle) {
        viewModel.refundTransaction(args.getString(TRANSACTION_ID))
    }

    private fun handleCancelTransaction(args: Bundle) {
        viewModel.cancelTransaction(args.getString(TRANSACTION_ID))
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
            viewModel.cancel()
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

    fun addEvent(message: String, method: String) {
        activityRef.get()?.let { activity ->
            activity.runOnUiThread {
                viewModel.addEvent(Event(message, method))
            }
        }
    }
}

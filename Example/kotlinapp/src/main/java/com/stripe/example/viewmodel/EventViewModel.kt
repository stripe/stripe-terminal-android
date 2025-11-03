package com.stripe.example.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stripe.example.TerminalRepository
import com.stripe.example.model.Event
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.models.AllowRedisplay
import com.stripe.stripeterminal.external.models.CollectPaymentIntentConfiguration
import com.stripe.stripeterminal.external.models.CollectRefundConfiguration
import com.stripe.stripeterminal.external.models.CollectSetupIntentConfiguration
import com.stripe.stripeterminal.external.models.ConfirmPaymentIntentConfiguration
import com.stripe.stripeterminal.external.models.CreateConfiguration
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentParameters
import com.stripe.stripeterminal.external.models.RefundParameters
import com.stripe.stripeterminal.external.models.SetupIntent
import com.stripe.stripeterminal.external.models.SetupIntentCancellationParameters
import com.stripe.stripeterminal.external.models.SetupIntentParameters
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.ktx.cancelPaymentIntent
import com.stripe.stripeterminal.ktx.cancelSetupIntent
import com.stripe.stripeterminal.ktx.createPaymentIntent
import com.stripe.stripeterminal.ktx.createSetupIntent
import com.stripe.stripeterminal.ktx.processPaymentIntent
import com.stripe.stripeterminal.ktx.processRefund
import com.stripe.stripeterminal.ktx.processSetupIntent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class EventViewModel(eventsList: List<Event> = mutableListOf()) : ViewModel() {
    private val eventList: ArrayList<Event> = ArrayList(eventsList)
    var events: MutableLiveData<List<Event>> = MutableLiveData(eventList)
    var isComplete: MutableLiveData<Boolean> = MutableLiveData(false)
    private val jobs = mutableListOf<Job>()

    fun addEvent(event: Event) {
        eventList.add(event)
        events.postValue(eventList)
    }

    fun cancel() {
        viewModelScope.launch {
            addEvent(Event("Cancel invoked", "viewModel.cancel"))
            jobs.filter { it.isActive }
                .forEach { it.cancel(CancellationException("Cancel invoked")) }
            jobs.joinAll()
            jobs.clear()
        }
    }

    fun takePayment(
        paymentParameters: PaymentIntentParameters,
        createConfiguration: CreateConfiguration?,
        collectConfiguration: CollectPaymentIntentConfiguration,
        confirmConfiguration: ConfirmPaymentIntentConfiguration
    ) {
        viewModelScopeSafeLaunch {
            Terminal.getInstance().run {
                val createdPI = createPaymentIntent(paymentParameters, createConfiguration)
                addEvent(Event("Created PaymentIntent", "terminal.createPaymentIntent"))
                // Track created PaymentIntents, for cancellation from the UI
                TerminalRepository.addPaymentIntent(createdPI)
                // Using a single step processPaymentIntent for collect and confirm,
                // Alternatively you could perform collect and confirm as separate steps
                // especially if you have business logic to perform between collect and confirm.
                val processedPI = processPaymentIntent(
                    intent = createdPI, collectConfig = collectConfiguration, confirmConfig = confirmConfiguration
                )
                addEvent(Event("Processed PaymentIntent", "terminal.processPaymentIntent"))
                TerminalRepository.addPaymentIntent(processedPI)
            }
        }.also(jobs::add)
    }

    fun saveCard(
        setupIntentParameters: SetupIntentParameters,
        allowRedisplay: AllowRedisplay,
        collectConfiguration: CollectSetupIntentConfiguration,
    ) {
        viewModelScopeSafeLaunch {
            Terminal.getInstance().run {
                val createdSI = createSetupIntent(setupIntentParameters)
                addEvent(Event("Created SetupIntent", "terminal.createSetupIntent"))
                // Keep track of created SetupIntents, for cancellation from the UI
                TerminalRepository.addSetupIntent(createdSI)
                // Using a single step process for collecting and confirming the setup intent.
                // Alternatively you could call collect and confirm separately if your integration has business logic
                // to perform between collect and confirm.
                val processedSI = processSetupIntent(
                    intent = createdSI,
                    allowRedisplay = allowRedisplay,
                    collectConfig = collectConfiguration,
                )
                TerminalRepository.addSetupIntent(processedSI)
                addEvent(Event("Processed SetupIntent", "terminal.processSetupIntent"))
            }
        }
    }

    /**
     * Safely launch a coroutine in the viewModelScope invoking [block] catching and posting [TerminalException]s as Events.
     * Also catches [CancellationException]s to ensure they are rethrown after logging the cancellation event.
     */
    private fun viewModelScopeSafeLaunch(block: suspend () -> Unit): Job {
        return viewModelScope.launch(Dispatchers.Default) {
            try {
                block()
            } catch (e: TerminalException) {
                addEvent(Event("${e.errorCode}", e.errorMessage))
            } catch (e: CancellationException) {
                addEvent(Event("Canceled", "viewModel.cancelIntents"))
                throw e
            } finally {
                isComplete.postValue(true)
            }
        }.also(jobs::add)
    }

    private fun cancelPaymentIntent(paymentIntent: PaymentIntent) {
        viewModelScopeSafeLaunch {
            val cancelledPI = Terminal.getInstance().cancelPaymentIntent(paymentIntent)
            addEvent(Event("Cancelled PaymentIntent", "terminal.cancelPaymentIntent"))
            TerminalRepository.addPaymentIntent(cancelledPI)
        }.also(jobs::add)
    }

    private fun refundPayment(paymentIntent: PaymentIntent) {
        viewModelScopeSafeLaunch {
            val refundParameters = RefundParameters.ByPaymentIntentId(
                id = paymentIntent.id!!,
                clientSecret = paymentIntent.clientSecret!!,
                amount = paymentIntent.amount,
                currency = paymentIntent.currency!!
            ).setMetadata(TerminalRepository.genMetaData()).build()
            val refund = Terminal.getInstance().processRefund(
                parameters = refundParameters, collectConfig = CollectRefundConfiguration.Builder().build()
            )
            addEvent(Event("Processed Refund", "terminal.processRefund"))
            TerminalRepository.addRefund(refund)
        }
    }

    private fun cancelSetupIntent(setupIntent: SetupIntent) {
        viewModelScopeSafeLaunch {
            val cancelledSI = Terminal.getInstance().cancelSetupIntent(
                params = SetupIntentCancellationParameters.NULL, setupIntent = setupIntent
            )
            addEvent(Event("Cancelled SetupIntent", "terminal.cancelSetupIntent"))
            TerminalRepository.addSetupIntent(cancelledSI)
        }
    }

    fun cancelTransaction(transactionId: String?) {
        if (transactionId == null) {
            addEvent(Event("No transactionId provided to cancel", "viewModel.cancelTransaction"))
            isComplete.postValue(true)
            return
        }
        // Look for a created PaymentIntent with the matching transactionId
        TerminalRepository.getPaymentIntentByTransactionId(transactionId)?.let { pi ->
            cancelPaymentIntent(pi)
            return
        }
        // Look for a created SetupIntent with the matching transactionId
        TerminalRepository.getSetupIntentByTransactionId(transactionId)?.let { si ->
            cancelSetupIntent(si)
            return
        }
        addEvent(Event("No matching PaymentIntent or SetupIntent found to cancel", "viewModel.cancelTransaction"))
        isComplete.postValue(true)
    }

    fun refundTransaction(transactionId: String?) {
        if (transactionId == null) {
            addEvent(Event("No transactionId provided to refund", "viewModel.refundTransaction"))
            isComplete.postValue(true)
            return
        }
        // Look for a created PaymentIntent with the matching transactionId
        TerminalRepository.getPaymentIntentByTransactionId(transactionId)?.let { pi ->
            refundPayment(pi)
            return
        }
        addEvent(Event("No matching PaymentIntent found to refund", "viewModel.refundTransaction"))
        isComplete.postValue(true)
    }

    override fun onCleared() {
        super.onCleared()
        jobs.clear()
    }
}

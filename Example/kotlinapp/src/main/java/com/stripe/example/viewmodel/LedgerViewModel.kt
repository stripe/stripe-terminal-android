package com.stripe.example.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.stripe.example.NavigationListener
import com.stripe.example.TerminalRepository
import com.stripe.example.model.LedgerEntry
import com.stripe.example.network.ApiClient
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.models.PaymentIntentStatus
import com.stripe.stripeterminal.ktx.retrievePaymentIntent
import com.stripe.stripeterminal.ktx.retrieveSetupIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LedgerViewModel : ViewModel() {
    var navigationListener: NavigationListener? = null
    private val loading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = loading
    private val jobs = mutableListOf<Job>()
    val entries: LiveData<List<LedgerEntry>> = TerminalRepository.run {
        combine(flow = paymentIntents, flow2 = setupIntents, flow3 = refunds) { payments, setups, refunds ->
            // associate the refunds by paymentIntentId for easy lookup
            val refundsByPaymentId = refunds.associateBy { it.paymentIntentId }
            val paymentEntries = payments.map { LedgerEntry.Payment(it, refundsByPaymentId[it.id]) }
            val cardEntries = setups.map { LedgerEntry.Card(it) }
            // sort entries by created date, descending
            // (most recent first)
            (paymentEntries + cardEntries).sortedByDescending { entry ->
                when (entry) {
                    is LedgerEntry.Payment -> entry.intent.created
                    is LedgerEntry.Card -> entry.intent.created
                }
            }
        }
    }.asLiveData(viewModelScope.coroutineContext)

    fun capture(entry: LedgerEntry.Payment) {
        viewModelScope.launch(Dispatchers.IO) {
            loading.postValue(true)
            // Capture the PaymentIntent if necessary,
            // ideally you want to move this logic to your backend
            // For example purposes only, we are doing it here
            entry.intent.takeIf { intent ->
                !intent.id.isNullOrEmpty() && intent.status == PaymentIntentStatus.REQUIRES_CAPTURE &&
                    intent.offlineDetails?.requiresUpload != true
            }?.let { intent ->
                ApiClient.capturePaymentIntent(intent.id!!)
                // Then refresh the payment entry so it has the latest state
                val updatedPI = Terminal.getInstance().retrievePaymentIntent(requireNotNull(intent.clientSecret))
                TerminalRepository.addPaymentIntent(updatedPI)
            }
            loading.postValue(false)
        }.also { jobs.add(it) }
    }

    fun cancel(entry: LedgerEntry) {
        navigationListener?.onRequestCancel(entry.uniqueId)
    }

    fun refund(entry: LedgerEntry.Payment) {
        navigationListener?.onRequestRefundPayment(entry.uniqueId)
    }

    fun refresh(entry: LedgerEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            loading.postValue(true)
            // Refresh the PaymentIntent or SetupIntent to get the latest state
            when (entry) {
                is LedgerEntry.Payment -> {
                    entry.intent.clientSecret?.let { secret ->
                        val updatedPI = Terminal.getInstance().retrievePaymentIntent(secret)
                        TerminalRepository.addPaymentIntent(updatedPI)
                    }
                }

                is LedgerEntry.Card -> {
                    entry.intent.clientSecret?.let { secret ->
                        val updatedSI = Terminal.getInstance().retrieveSetupIntent(secret)
                        TerminalRepository.addSetupIntent(updatedSI)
                    }
                }
            }
            loading.postValue(false)
        }.also { jobs.add(it) }
    }
}

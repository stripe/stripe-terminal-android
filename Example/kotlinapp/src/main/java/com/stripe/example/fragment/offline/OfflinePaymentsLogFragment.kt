package com.stripe.example.fragment.offline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.TerminalOfflineListener
import com.stripe.example.fragment.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OfflinePaymentsLogFragment : Fragment() {
    private val adapter: OfflinePaymentsLogAdapter = OfflinePaymentsLogAdapter()
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_offline_payments_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchAndRepeatWithViewLifecycle(Lifecycle.State.RESUMED) {
            launch {
                TerminalOfflineListener.offlineEventsFlow
                        .collectLatest {
                            adapter.updateOfflineLogs(it)
                        }
            }
        }

        view.findViewById<View>(R.id.done_button).setOnClickListener {
            (activity as? NavigationListener)?.onRequestExitWorkflow()
        }

        view.findViewById<RecyclerView>(R.id.offline_logs_recycler_view)?.run {
            this.layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@OfflinePaymentsLogFragment.adapter
        }
    }

    companion object {
        const val TAG = "com.stripe.example.fragment.OfflinePaymentsLogFragment"
    }
}

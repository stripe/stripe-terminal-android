package com.stripe.example


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_event.view.*
import kotlinx.android.synthetic.main.log_event_layout.view.*

/**
 * The `EventFragment` displays events as they happen during a payment flow
 */
class EventFragment : Fragment() {

    private var cancelButton: TextView? = null
    private var doneButton: TextView? = null
    private var eventContainer: LinearLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event, container, false)

        cancelButton = view.cancel_button
        doneButton = view.done_button
        eventContainer = view.event_container

        cancelButton?.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onRequestCancelCollectPaymentMethod()
            }
        }

        doneButton?.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onRequestExitWorkflow()
            }
        }

        return view
    }

    fun completeFlow() {
        context?.let {
            cancelButton?.setTextColor(ContextCompat.getColor(it, R.color.colorPrimaryDark))
            doneButton?.visibility = View.VISIBLE
        }
    }

    fun displayEvent(message: String, method: String) {
        if (eventContainer != null) {
            val eventDisplay = layoutInflater.inflate(R.layout.log_event_layout, eventContainer, false)
            eventDisplay.message.text = message
            eventDisplay.method.text = method
            eventContainer?.addView(eventDisplay)
        }
    }
}

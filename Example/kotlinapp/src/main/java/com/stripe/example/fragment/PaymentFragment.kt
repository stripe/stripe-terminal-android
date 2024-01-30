package com.stripe.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.model.OfflineBehaviorSelection
import java.text.NumberFormat
import java.util.Locale

/**
 * The `PaymentFragment` allows the user to create a custom payment and ask the reader to handle it.
 */
class PaymentFragment : Fragment() {

    companion object {
        const val TAG = "com.stripe.example.fragment.PaymentFragment"
    }

    private var selectedBehavior = OfflineBehaviorSelection.DEFAULT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_payment, container, false)
        val amountEditText = view.findViewById<TextView>(R.id.amount_edit_text)
        val chargeAmount = view.findViewById<TextView>(R.id.charge_amount)
        val currentEditText = view.findViewById<EditText>(R.id.currency_edit_text)
        val skipTippingSwitch = view.findViewById<SwitchMaterial>(R.id.skip_tipping_switch)
        val extendedAuthSwitch = view.findViewById<SwitchMaterial>(R.id.extended_auth_switch)
        val incrementalAuthSwitch = view.findViewById<SwitchMaterial>(R.id.incremental_auth_switch)

        amountEditText.doAfterTextChanged { editable ->
            if (editable.toString().isNotEmpty()) {
                chargeAmount.text = formatCentsToString(editable.toString().toInt())
            }
        }

        view.findViewById<View>(R.id.collect_payment_button).setOnClickListener {
            val skipTipping = skipTippingSwitch.isChecked
            val extendedAuth = extendedAuthSwitch.isChecked
            val incrementalAuth = incrementalAuthSwitch.isChecked

            (activity as? NavigationListener)?.onRequestPayment(
                amountEditText.text.toString().toLong(),
                currentEditText.text.toString(),
                skipTipping,
                extendedAuth,
                incrementalAuth,
                selectedBehavior,
            )
        }

        view.findViewById<View>(R.id.home_button).setOnClickListener {
            (activity as? NavigationListener)?.onRequestExitWorkflow()
        }

        val spinner = view.findViewById<Spinner>(R.id.offline_behavior_spinner)
        val offlineBehaviorOptions = OfflineBehaviorSelection.entries.map {
            resources.getString(it.labelResource)
        }
        val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                offlineBehaviorOptions
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                selectedBehavior = OfflineBehaviorSelection.entries[i]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        return view
    }

    private fun formatCentsToString(i: Int): String {
        return NumberFormat.getCurrencyInstance(Locale.US).format(i / 100.0)
    }
}

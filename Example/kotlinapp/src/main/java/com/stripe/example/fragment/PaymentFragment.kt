package com.stripe.example.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.stripe.example.NavigationListener
import com.stripe.example.R
import java.text.NumberFormat
import java.util.Locale
import kotlinx.android.synthetic.main.fragment_payment.view.amount_edit_text
import kotlinx.android.synthetic.main.fragment_payment.view.charge_amount
import kotlinx.android.synthetic.main.fragment_payment.view.collect_payment_button
import kotlinx.android.synthetic.main.fragment_payment.view.currency_edit_text
import kotlinx.android.synthetic.main.fragment_payment.view.home_button

/**
 * The `PaymentFragment` allows the user to create a custom payment and ask the reader to handle it.
 */
class PaymentFragment : Fragment() {

    companion object {
        const val TAG = "com.stripe.example.fragment.PaymentFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        view.amount_edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable.toString().isNotEmpty()) {
                    view.charge_amount.text = formatCentsToString(editable.toString().toInt())
                }
            }
        })

        view.collect_payment_button.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onRequestPayment(
                        view.amount_edit_text.text.toString().toInt(),
                        view.currency_edit_text.text.toString())
            }
        }

        view.home_button.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onRequestExitWorkflow()
            }
        }

        return view
    }

    private fun formatCentsToString(i: Int): String {
        return NumberFormat.getCurrencyInstance(Locale.US).format(i / 100.0)
    }
}

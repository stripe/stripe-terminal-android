package com.stripe.example.javaapp.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.stripe.example.javaapp.NavigationListener;
import com.stripe.example.javaapp.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * The `PaymentFragment` allows the user to create a custom payment and ask the reader to handle it.
 */
public class PaymentFragment extends Fragment {

    @NotNull public static final String TAG = "com.stripe.example.fragment.PaymentFragment";

    @Nullable
    @Override
    public View onCreateView(
            @NotNull LayoutInflater layoutInflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        final View view = layoutInflater.inflate(R.layout.fragment_payment, container, false);

        ((TextView) view.findViewById(R.id.amount_edit_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                final String editableString = editable.toString();
                if (!editableString.isEmpty()) {
                    ((TextView) view.findViewById(R.id.charge_amount))
                            .setText(formatCentsToString(Integer.parseInt(editable.toString())));
                }
            }
        });

        view.findViewById(R.id.collect_payment_button).setOnClickListener(v -> {
            final FragmentActivity activity = getActivity();
            if (activity instanceof NavigationListener) {
                final String amount = ((TextView) view.findViewById(R.id.amount_edit_text))
                        .getText().toString();
                final String currency = ((TextView) view.findViewById(R.id.currency_edit_text))
                        .getText().toString();
                ((NavigationListener) activity).onRequestPayment(Integer.parseInt(amount), currency);
            }
        });

        view.findViewById(R.id.home_button).setOnClickListener(v -> {
            final FragmentActivity activity = getActivity();
            if (activity instanceof NavigationListener) {
                ((NavigationListener) activity).onRequestExitWorkflow();
            }
        });

        return view;
    }

    private String formatCentsToString(int cents) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(cents / 100.0);
    }
}

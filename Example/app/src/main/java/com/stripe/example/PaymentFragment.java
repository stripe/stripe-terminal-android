package com.stripe.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.stripeterminal.Callback;
import com.stripe.stripeterminal.Cancelable;
import com.stripe.stripeterminal.PaymentIntentParameters;
import com.stripe.stripeterminal.PaymentStatus;
import com.stripe.stripeterminal.ReaderInputListener;
import com.stripe.stripeterminal.ReaderInputOptions;
import com.stripe.stripeterminal.ReaderInputPrompt;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.PaymentIntentCallback;
import com.stripe.stripeterminal.PaymentIntent;
import com.stripe.stripeterminal.TerminalException;
import com.stripe.stripeterminal.TerminalListener;

import javax.annotation.Nonnull;

/**
 * The {@code PaymentFragment} is where the user can kick off the process of creating a charge on
 * the connected reader. They can change the amount and currency of the charge, as well as cancel
 * payment method collection.
 */
public class PaymentFragment extends Fragment implements TerminalListener {

    private EditText amountEditText;
    private EditText currencyEditText;
    private TextView paymentStatus;
    private Button startButton;
    private Button cancelButton;

    private Terminal terminal;
    private Cancelable cancelable;

    public PaymentFragment() {
        // Register the fragment for Terminal updates
        TerminalEventListener.registerListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        // Create a Terminal instance (or use the existing one)
        terminal = TerminalProvider.getInstance(getActivity());

        // Prep the input fields
        amountEditText = view.findViewById(R.id.amount_edit_text);
        currencyEditText = view.findViewById(R.id.currency_edit_text);

        // When the start button is clicked, kick off a payment by creating a PaymentIntent
        startButton = view.findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> {
            int amount = Integer.valueOf(amountEditText.getText().toString());
            String currency = currencyEditText.getText().toString();

            PaymentIntentParameters params = new PaymentIntentParameters.Builder()
                    .setAmount(amount)
                    .setCurrency(currency.toLowerCase())
                    .build();
            terminal.createPaymentIntent(params, new CreatePaymentIntentCallback());
        });

        // Set the PaymentStatus to the current status
        paymentStatus = view.findViewById(R.id.payment_status);
        paymentStatus.setText(String.format(getString(R.string.payment_status_label),
                TerminalEventListener.getCurrentPaymentStatus().toString()));

        // The cancel button allows for cancellation of the collectPaymentMethod operation
        cancelButton = view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            if (cancelable != null) {
                cancelable.cancel(new Callback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getActivity(), R.string.canceled, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@Nonnull TerminalException e) {
                        Log.e(getClass().getSimpleName(), e.getErrorMessage(), e);
                    }
                });
            }
        });

        return view;
    }

    /**
     * Listen for PaymentStatus changes so that we can update the payment status text
     * @param status The new payment status
     */
    @Override
    public void onPaymentStatusChange(PaymentStatus status) {
        getActivity().runOnUiThread(() ->
                paymentStatus.setText(String.format(getString(R.string.payment_status_label),
                        status.toString())));
    }

    /**
     * A simple callback that will run after a payment intent is confirmed
     */
    private class ConfirmPaymentIntentCallback implements PaymentIntentCallback {

        @Override
        public void onSuccess(@Nonnull PaymentIntent paymentIntent) {
            // If we have a confirmed payment intent, we can show the PaymentIntentActivity
            Intent intent = PaymentIntentActivity.createIntent(getActivity(), paymentIntent);
            startActivity(intent);
        }

        @Override
        public void onFailure(@Nonnull TerminalException e) {
            Log.e(getClass().getSimpleName(), e.getErrorMessage(), e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TerminalEventListener.deregisterListener(this);
    }

    /**
     * A simple callback that will run after a payment method is collected
     */
    private class CollectPaymentMethodCallback implements PaymentIntentCallback {

        @Override
        public void onSuccess(@Nonnull PaymentIntent paymentIntent) {
            // If we've collected a payment method, it's time to confirm the payment intent
            terminal.confirmPaymentIntent(paymentIntent, new ConfirmPaymentIntentCallback());
        }

        @Override
        public void onFailure(@Nonnull TerminalException e) {
            Log.e(getClass().getSimpleName(), e.getErrorMessage(), e);
        }
    }

    /**
     * A simple callback that will run after a payment intent has been created
     */
    private class CreatePaymentIntentCallback implements PaymentIntentCallback {

        @Override
        public void onSuccess(@Nonnull PaymentIntent paymentIntent) {
            // After we have a new payment intent, we should try to collect a payment method
            // We'll also save the Cancelable object so that we can cancel collection if needed
            cancelable = terminal.collectPaymentMethod(paymentIntent, new ReaderListener(),
                    new CollectPaymentMethodCallback());
        }

        @Override
        public void onFailure(@Nonnull TerminalException e) {
            Log.e(getClass().getSimpleName(), e.getErrorMessage(), e);
        }
    }

    /**
     * A simple listener object that will just surface reader events to the user as Toasts.
     */
    private class ReaderListener implements ReaderInputListener {

        @Override
        public void onBeginWaitingForReaderInput(ReaderInputOptions options) {
            Toast.makeText(getActivity(), options.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestReaderInputPrompt(ReaderInputPrompt prompt) {
            Toast.makeText(getActivity(), prompt.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}

package com.stripe.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.stripe.stripeterminal.PaymentIntentCallback;
import com.stripe.stripeterminal.PaymentIntent;
import com.stripe.stripeterminal.TerminalException;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The {@code PaymentIntentActivity} shows the payment intent object that has been used for the
 * most recent payment. It allows for examination of the raw JSON returned from Stripe as well as
 * cancellation of the intent if necessary.
 */
public class PaymentIntentActivity extends AppCompatActivity {

    private static final String PAYMENT_INTENT = "com.stripe.example.payment_intent";

    private Button cancelButton;
    private TextView paymentIntentContent;
    private TextView paymentIntentStatus;

    private PaymentIntent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_intent);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        paymentIntentStatus = findViewById(R.id.intent_status);
        paymentIntentContent = findViewById(R.id.intent_content);

        // Surface the current status of the payment intent
        this.intent = getIntent().getExtras().getParcelable(PAYMENT_INTENT);
        if (intent != null) {
            paymentIntentContent.setText(prettyPrintJson(intent.getRawJson()));
            paymentIntentStatus.setText(String.format(getString(R.string.intent_status_label),
                    getString(R.string.capture_required)));
        }

        // If the cancellation button is clicked, cancel the current payment intent
        cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(view -> TerminalProvider.getInstance(getApplicationContext())
                .cancelPaymentIntent(intent,
                        new PaymentIntentCallback() {
                            @Override
                            public void onFailure(@Nonnull TerminalException e) {
                                Log.e(getClass().getSimpleName(), e.getErrorMessage(), e);
                            }

                            @Override
                            public void onSuccess(final PaymentIntent paymentIntent) {
                                runOnUiThread(() -> {
                                    if (paymentIntent != null && paymentIntent.getStatus() == PaymentIntent.PaymentIntentStatus.CANCELED) {
                                        // If we successfully cancelled, update the UI
                                        paymentIntentStatus.setText(String.format(getString(R.string.intent_status_label), getString(R.string.canceled)));
                                        paymentIntentContent.setText(prettyPrintJson(paymentIntent.getRawJson()));
                                    }
                                });
                            }
                        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.close_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_close) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Create an Intent for this activity class
     * @param context The current Context
     * @param paymentIntent The PaymentIntent that should be passed int
     * @return An Intent that will create this activity
     */
    public static Intent createIntent(Context context, PaymentIntent paymentIntent) {
        Intent intent = new Intent(context, PaymentIntentActivity.class);
        intent.putExtra(PAYMENT_INTENT, paymentIntent);
        return intent;
    }

    // Utility function to pretty print the payment intent
    private String prettyPrintJson(String json) {
        try {
            return new JSONObject(json).toString(2);
        } catch (JSONException e) {
            return e.toString();
        }
    }
}

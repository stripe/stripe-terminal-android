package com.stripe.example.javaapp.fragment.event;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.NavigationListener;
import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.model.Event;
import com.stripe.example.javaapp.network.ApiClient;
import com.stripe.example.javaapp.viewmodel.EventViewModel;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.BluetoothReaderListener;
import com.stripe.stripeterminal.external.callable.Callback;
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback;
import com.stripe.stripeterminal.external.callable.PaymentMethodCallback;
import com.stripe.stripeterminal.external.models.PaymentIntent;
import com.stripe.stripeterminal.external.models.PaymentIntentParameters;
import com.stripe.stripeterminal.external.models.PaymentMethod;
import com.stripe.stripeterminal.external.models.ReadReusableCardParameters;
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage;
import com.stripe.stripeterminal.external.models.ReaderEvent;
import com.stripe.stripeterminal.external.models.ReaderInputOptions;
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate;
import com.stripe.stripeterminal.external.models.TerminalException;
import com.stripe.stripeterminal.external.callable.Cancelable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * The `EventFragment` displays events as they happen during a payment flow
 */
public class EventFragment extends Fragment implements BluetoothReaderListener {

    @NotNull public static final String TAG = "com.stripe.example.fragment.event.EventFragment";

    @NotNull private static final String AMOUNT =
            "com.stripe.example.fragment.event.EventFragment.amount";
    @NotNull private static final String CURRENCY =
            "com.stripe.example.fragment.event.EventFragment.currency";
    @NotNull private static final String REQUEST_PAYMENT =
            "com.stripe.example.fragment.event.EventFragment.request_payment";
    @NotNull private static final String READ_REUSABLE_CARD =
            "com.stripe.example.fragment.event.EventFragment.read_reusable_card";

    public static EventFragment readReusableCard() {
        final EventFragment fragment = new EventFragment();
        final Bundle bundle = new Bundle();
        bundle.putBoolean(READ_REUSABLE_CARD, true);
        bundle.putBoolean(REQUEST_PAYMENT, false);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static EventFragment requestPayment(int amount, @NotNull String currency) {
        final EventFragment fragment = new EventFragment();
        final Bundle bundle = new Bundle();
        bundle.putInt(AMOUNT, amount);
        bundle.putString(CURRENCY, currency);
        bundle.putBoolean(REQUEST_PAYMENT, true);
        bundle.putBoolean(READ_REUSABLE_CARD, false);
        fragment.setArguments(bundle);
        return fragment;
    }

    private EventAdapter adapter;
    private WeakReference<FragmentActivity> activityRef;

    private EventViewModel viewModel;

    private PaymentIntent paymentIntent;

    @NotNull private final PaymentIntentCallback processPaymentCallback = new PaymentIntentCallback() {
        @Override
        public void onSuccess(@NotNull PaymentIntent paymentIntent) {
            addEvent("Processed payment", "terminal.processPayment");
            try {
                ApiClient.capturePaymentIntent(paymentIntent.getId());
                addEvent("Captured PaymentIntent", "backend.capturePaymentIntent");
                completeFlow();
            } catch (IOException e) {
                Log.e("StripeExample", e.getMessage(), e);
                completeFlow();
            }
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            EventFragment.this.onFailure(e);
        }
    };

    @NotNull private final PaymentIntentCallback cancelPaymentIntentCallback = new PaymentIntentCallback() {
        @Override
        public void onSuccess(@NotNull PaymentIntent paymentIntent) {
            addEvent("Canceled PaymentIntent", "terminal.cancelPaymentIntent");
            final FragmentActivity activity = activityRef.get();
            if (activity instanceof NavigationListener) {
                activity.runOnUiThread(((NavigationListener) activity)::onCancelCollectPaymentMethod);
            }
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            EventFragment.this.onFailure(e);
        }
    };

    @NotNull private final PaymentIntentCallback collectPaymentMethodCallback = new PaymentIntentCallback() {
        @Override
        public void onSuccess(@NotNull PaymentIntent paymentIntent) {
            addEvent("Collected PaymentMethod", "terminal.collectPaymentMethod");
            Terminal.getInstance().processPayment(paymentIntent, processPaymentCallback);
            viewModel.collectTask = null;
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            EventFragment.this.onFailure(e);
        }
    };

    @NotNull private final PaymentIntentCallback createPaymentIntentCallback = new PaymentIntentCallback() {
        @Override
        public void onSuccess(@NotNull PaymentIntent intent) {
            paymentIntent = intent;
            addEvent("Created PaymentIntent", "terminal.createPaymentIntent");
            viewModel.collectTask = Terminal.getInstance().collectPaymentMethod(
                    paymentIntent, collectPaymentMethodCallback);
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            EventFragment.this.onFailure(e);
        }
    };

    @NotNull private final PaymentMethodCallback reusablePaymentMethodCallback = new PaymentMethodCallback() {
        @Override
        public void onSuccess(@NotNull PaymentMethod paymentMethod) {
            addEvent("Created PaymentMethod: ${paymentMethod.id}", "terminal.readReusableCard");
            completeFlow();
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            EventFragment.this.onFailure(e);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityRef = new WeakReference<>(getActivity());
        viewModel = new ViewModelProvider(this).get(EventViewModel.class);

        if (savedInstanceState == null) {
            final Bundle arguments = getArguments();
            if (arguments != null) {
                if (arguments.getBoolean(REQUEST_PAYMENT)) {
                    final String currency = arguments.getString(CURRENCY);
                    final PaymentIntentParameters params = new PaymentIntentParameters.Builder()
                            .setAmount(arguments.getInt(AMOUNT))
                            .setCurrency(currency != null ? currency.toLowerCase(Locale.ENGLISH) : "usd")
                            .build();
                    Terminal.getInstance().createPaymentIntent(params, createPaymentIntentCallback);
                } else if (arguments.getBoolean(READ_REUSABLE_CARD)) {
                    viewModel.collectTask = Terminal
                            .getInstance()
                            .readReusableCard(
                                    ReadReusableCardParameters.Companion.getNULL(),
                                    reusablePaymentMethodCallback);
                }
            }
        }
    }

    @Nullable
    public View onCreateView(
        @NotNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView eventRecyclerView = view.findViewById(R.id.event_recycler_view);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new EventAdapter();
        eventRecyclerView.setAdapter(adapter);

        view.findViewById(R.id.cancel_button).setOnClickListener(v -> {
            if (viewModel.collectTask != null) {
                viewModel.collectTask.cancel(new Callback() {
                    @Override
                    public void onSuccess() {
                        viewModel.collectTask = null;
                        if (paymentIntent != null) {
                            Terminal.getInstance().cancelPaymentIntent(paymentIntent, cancelPaymentIntentCallback);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull TerminalException e) {
                        viewModel.collectTask = null;
                        EventFragment.this.onFailure(e);
                    }
                });
            }
        });

        view.findViewById(R.id.done_button).setOnClickListener(v -> {
            final FragmentActivity activity = activityRef.get();
            if (activity instanceof NavigationListener) {
                activity.runOnUiThread(((NavigationListener) activity)::onRequestExitWorkflow);
            }
        });

        viewModel.isComplete.observe(getViewLifecycleOwner(), isComplete -> {
            ((TextView) view.findViewById(R.id.cancel_button))
                    .setTextColor(ContextCompat.getColor(getContext(),
                            isComplete ? R.color.colorPrimaryDark : R.color.colorAccent));

            view.findViewById(R.id.done_button).setVisibility(isComplete ? View.VISIBLE : View.GONE);
        });

        viewModel.events.observe(getViewLifecycleOwner(), events -> adapter.updateEvents(events));
    }

    @Override
    public void onRequestReaderDisplayMessage(@NotNull ReaderDisplayMessage message) {
        addEvent(message.toString(), "listener.onRequestReaderDisplayMessage");
    }

    @Override
    public void onRequestReaderInput(@NotNull ReaderInputOptions options) {
        addEvent(options.toString(), "listener.onRequestReaderInput");
    }

    private void completeFlow() {
        final FragmentActivity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(() -> viewModel.isComplete.setValue(true));
        }
    }

    private void addEvent(@NotNull String message, @NotNull String method) {
        final FragmentActivity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(() -> viewModel.addEvent(new Event(message, method)));
        }
    }

    private void onFailure(@NotNull TerminalException e) {
        addEvent(e.getErrorMessage(), e.getErrorCode().toString());
        completeFlow();
    }

    // Unused overrides
    @Override
    public void onStartInstallingUpdate(@NotNull ReaderSoftwareUpdate update, @Nullable Cancelable cancelable) { }

    @Override
    public void onReportReaderSoftwareUpdateProgress(float progress) { }

    @Override
    public void onFinishInstallingUpdate(@Nullable ReaderSoftwareUpdate update, @Nullable TerminalException e) { }

    @Override
    public void onReportAvailableUpdate(@NotNull ReaderSoftwareUpdate update) { }

    @Override
    public void onReportReaderEvent(@NotNull ReaderEvent event) { }

    @Override
    public void onReportLowBatteryWarning() { }
}

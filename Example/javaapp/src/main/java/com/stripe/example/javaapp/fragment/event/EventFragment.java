package com.stripe.example.javaapp.fragment.event;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.NavigationListener;
import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.model.Event;
import com.stripe.example.javaapp.model.OfflineBehaviorSelection;
import com.stripe.example.javaapp.network.ApiClient;
import com.stripe.example.javaapp.viewmodel.EventViewModel;
import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.InternalApi;
import com.stripe.stripeterminal.external.callable.Callback;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.MobileReaderListener;
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback;
import com.stripe.stripeterminal.external.callable.SetupIntentCallback;
import com.stripe.stripeterminal.external.models.AllowRedisplay;
import com.stripe.stripeterminal.external.models.BatteryStatus;
import com.stripe.stripeterminal.external.models.CardPresentParameters;
import com.stripe.stripeterminal.external.models.CollectPaymentIntentConfiguration;
import com.stripe.stripeterminal.external.models.ConfirmPaymentIntentConfiguration;
import com.stripe.stripeterminal.external.models.CreateConfiguration;
import com.stripe.stripeterminal.external.models.DisconnectReason;
import com.stripe.stripeterminal.external.models.PaymentIntent;
import com.stripe.stripeterminal.external.models.PaymentIntentParameters;
import com.stripe.stripeterminal.external.models.PaymentMethodOptionsParameters;
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage;
import com.stripe.stripeterminal.external.models.ReaderEvent;
import com.stripe.stripeterminal.external.models.ReaderInputOptions;
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate;
import com.stripe.stripeterminal.external.models.SetupIntent;
import com.stripe.stripeterminal.external.models.SetupIntentCancellationParameters;
import com.stripe.stripeterminal.external.models.CollectSetupIntentConfiguration;
import com.stripe.stripeterminal.external.models.SetupIntentParameters;
import com.stripe.stripeterminal.external.models.TerminalException;
import com.stripe.stripeterminal.external.models.TippingConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * The `EventFragment` displays events as they happen during a payment flow
 */
public class EventFragment extends Fragment implements MobileReaderListener {

    @NotNull
    public static final String TAG = "com.stripe.example.fragment.event.EventFragment";

    @NotNull
    private static final String AMOUNT =
            "com.stripe.example.fragment.event.EventFragment.amount";
    @NotNull
    private static final String CURRENCY =
            "com.stripe.example.fragment.event.EventFragment.currency";
    @NotNull
    private static final String REQUEST_PAYMENT =
            "com.stripe.example.fragment.event.EventFragment.request_payment";
    @NotNull
    private static final String SAVE_CARD =
            "com.stripe.example.fragment.event.EventFragment.save_card";
    @NotNull
    private static final String SKIP_TIPPING =
            "com.stripe.example.fragment.event.EventFragment.skip_tipping";
    @NotNull
    private static final String EXTENDED_AUTH =
            "com.stripe.example.fragment.event.EventFragment.extended_auth";
    @NotNull
    private static final String INCREMENTAL_AUTH =
            "com.stripe.example.fragment.event.EventFragment.incremental_auth";

    @NotNull
    private static final String OFFLINE_BEHAVIOR =
            "com.stripe.example.fragment.event.EventFragment.offline_behavior";

    public static EventFragment collectSetupIntentPaymentMethod() {
        final EventFragment fragment = new EventFragment();
        final Bundle bundle = new Bundle();
        bundle.putBoolean(SAVE_CARD, true);
        bundle.putBoolean(REQUEST_PAYMENT, false);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static EventFragment requestPayment(
            long amount,
            @NotNull String currency,
            boolean skipTipping,
            boolean extendedAuth,
            boolean incrementalAuth,
            OfflineBehaviorSelection offlineBehavior
    ) {
        final EventFragment fragment = new EventFragment();
        final Bundle bundle = new Bundle();
        bundle.putLong(AMOUNT, amount);
        bundle.putString(CURRENCY, currency);
        bundle.putBoolean(REQUEST_PAYMENT, true);
        bundle.putBoolean(SAVE_CARD, false);
        bundle.putBoolean(SKIP_TIPPING, skipTipping);
        bundle.putBoolean(EXTENDED_AUTH, extendedAuth);
        bundle.putBoolean(INCREMENTAL_AUTH, incrementalAuth);
        bundle.putSerializable(OFFLINE_BEHAVIOR, offlineBehavior);
        fragment.setArguments(bundle);
        return fragment;
    }

    private EventAdapter adapter;
    private WeakReference<FragmentActivity> activityRef;

    private EventViewModel viewModel;

    private PaymentIntent paymentIntent;
    private SetupIntent setupIntent;

    @NotNull private final PaymentIntentCallback processPaymentIntentCallback = new PaymentIntentCallback() {
        @Override
        public void onSuccess(@NotNull PaymentIntent paymentIntent) {
            addEvent("Payment processing successful", "terminal.processPaymentIntent");
            viewModel.processTask = null;
            String paymentIntentId = paymentIntent.getId();
            if (paymentIntentId != null) {
                try {
                    ApiClient.capturePaymentIntent(paymentIntentId);
                    addEvent("Captured PaymentIntent", "backend.capturePaymentIntent");
                } catch (IOException e) {
                    Log.e("StripeExample", e.getMessage(), e);
                }
            }
            completeFlow();
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
            completeFlow();
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            EventFragment.this.onFailure(e);
        }
    };

    @NotNull private final PaymentIntentCallback createPaymentIntentCallback = new PaymentIntentCallback() {
        @OptIn(markerClass = InternalApi.class)
        @Override
        public void onSuccess(@NotNull PaymentIntent intent) {
            paymentIntent = intent;
            addEvent("Created PaymentIntent", "terminal.createPaymentIntent");

            final Bundle arguments = getArguments();
            final boolean skipTipping = (arguments != null) && arguments.getBoolean(SKIP_TIPPING);
            final CollectPaymentIntentConfiguration collectConfig = new CollectPaymentIntentConfiguration.Builder()
                    .skipTipping(skipTipping)
                    .setTippingConfiguration(
                            new TippingConfiguration.Builder().build()
                    ).build();
            viewModel.processTask = Terminal.getInstance().processPaymentIntent(
                    paymentIntent, collectConfig, new ConfirmPaymentIntentConfiguration.Builder().build(), processPaymentIntentCallback);
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            EventFragment.this.onFailure(e);
        }
    };

    @NotNull private final SetupIntentCallback createSetupIntentCallback = new SetupIntentCallback() {
        @Override
        public void onSuccess(@NotNull SetupIntent intent) {
            setupIntent = intent;
            addEvent("Created SetupIntent", "terminal.createSetupIntent");
            viewModel.processTask = Terminal.getInstance().processSetupIntent(
                    setupIntent, AllowRedisplay.ALWAYS, new CollectSetupIntentConfiguration.Builder().build(), processSetupIntentCallback);
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            EventFragment.this.onFailure(e);
        }
    };

    @NotNull private final SetupIntentCallback processSetupIntentCallback = new SetupIntentCallback() {
        @Override
        public void onSuccess(@NotNull SetupIntent setupIntent) {
            addEvent("Setup intent processing successful", "terminal.processSetupIntent");
            viewModel.processTask = null;
            completeFlow();
        }

        @Override
        public void onFailure(@NotNull TerminalException e) {
            EventFragment.this.onFailure(e);
        }
    };

    @NotNull private final SetupIntentCallback cancelSetupIntentCallback = new SetupIntentCallback() {
        @Override
        public void onSuccess(@NotNull SetupIntent setupIntent) {
            addEvent("Canceled SetupIntent", "terminal.cancelSetupIntent");
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
                    final String currency = arguments.getString(CURRENCY) != null ? arguments.getString(CURRENCY).toLowerCase(Locale.ENGLISH) : "usd";
                    final boolean extendedAuth = arguments.getBoolean(EXTENDED_AUTH);
                    final boolean incrementalAuth = arguments.getBoolean(INCREMENTAL_AUTH);
                    CardPresentParameters.Builder cardPresentParametersBuilder = new CardPresentParameters.Builder();
                    if (extendedAuth) {
                        cardPresentParametersBuilder.setRequestExtendedAuthorization(true);
                    }
                    if (incrementalAuth) {
                        cardPresentParametersBuilder.setRequestIncrementalAuthorizationSupport(true);
                    }

                    PaymentMethodOptionsParameters paymentMethodOptionsParameters = new PaymentMethodOptionsParameters.Builder()
                            .setCardPresentParameters(cardPresentParametersBuilder.build())
                            .build();

                    final PaymentIntentParameters params = new PaymentIntentParameters.Builder()
                            .setAmount(arguments.getLong(AMOUNT))
                            .setCurrency(currency)
                            .setPaymentMethodOptionsParameters(paymentMethodOptionsParameters)
                            .build();

                    OfflineBehaviorSelection offlineBehaviorSelection = (OfflineBehaviorSelection) arguments.getSerializable(OFFLINE_BEHAVIOR);
                    if (offlineBehaviorSelection == null) {
                        offlineBehaviorSelection = OfflineBehaviorSelection.DEFAULT;
                    }
                    final CreateConfiguration config = new CreateConfiguration(offlineBehaviorSelection.offlineBehavior);
                    Terminal.getInstance().createPaymentIntent(params, createPaymentIntentCallback, config);
                } else if (arguments.getBoolean(SAVE_CARD)) {
                    SetupIntentParameters params = new SetupIntentParameters.Builder().build();
                    Terminal.getInstance().createSetupIntent(params, createSetupIntentCallback);
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
            if (viewModel.processTask != null) {
                viewModel.processTask.cancel(new Callback() {
                    @Override
                    public void onSuccess() {
                        viewModel.processTask = null;
                        if (paymentIntent != null) {
                            Terminal.getInstance().cancelPaymentIntent(paymentIntent, cancelPaymentIntentCallback);
                        }
                        if (setupIntent != null) {
                            SetupIntentCancellationParameters params = new SetupIntentCancellationParameters.Builder().build();
                            Terminal.getInstance().cancelSetupIntent(setupIntent, params, cancelSetupIntentCallback);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull TerminalException e) {
                        viewModel.processTask = null;
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

        viewModel.events.observe(getViewLifecycleOwner(), events -> {
            adapter.updateEvents(events);
            eventRecyclerView.scrollToPosition(events.size() - 1);
        });
    }

    @Override
    public void onRequestReaderDisplayMessage(@NotNull ReaderDisplayMessage message) {
        addEvent(message.toString(), "listener.onRequestReaderDisplayMessage");
    }

    @Override
    public void onRequestReaderInput(@NotNull ReaderInputOptions options) {
        addEvent(options.toString(), "listener.onRequestReaderInput");
    }

    @Override
    public void onDisconnect(@NonNull DisconnectReason reason) {
        addEvent(reason.name(), "listener.onDisconnect");
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
        viewModel.processTask = null;
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

    @Override
    public void onBatteryLevelUpdate(float batteryLevel, @NonNull BatteryStatus batteryStatus, boolean isCharging) { }
}

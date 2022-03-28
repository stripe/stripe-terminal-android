package com.stripe.example.javaapp.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.stripe.example.javaapp.NavigationListener;
import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.databinding.FragmentTerminalBinding;
import com.stripe.example.javaapp.viewmodel.TerminalViewModel;
import com.stripe.stripeterminal.external.models.DiscoveryMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The `TerminalFragment` is the main [Fragment] shown in the app, and handles navigation to any
 * other [Fragment]s as necessary.
 */
public class TerminalFragment extends Fragment {

    public static final String TAG = "com.stripe.example.fragment.TerminalFragment";
    private static final String SIMULATED_SWITCH = "simulated_switch";
    private static final String DISCOVERY_METHOD = "discovery_method";

    private List<DiscoveryMethod> discoveryMethods = new ArrayList<>();
    private TerminalViewModel viewModel;

    public static DiscoveryMethod getCurrentDiscoveryMethod(Activity activity) {
        int pos = activity.getSharedPreferences(TAG, Context.MODE_PRIVATE)
                .getInt(DISCOVERY_METHOD, 0);
        return DiscoveryMethod.values()[pos];
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        discoveryMethods.add(DiscoveryMethod.BLUETOOTH_SCAN);
        discoveryMethods.add(DiscoveryMethod.INTERNET);
        discoveryMethods.add(DiscoveryMethod.USB);

        if (getArguments() != null) {
            viewModel = new TerminalViewModel(getArguments().getBoolean(SIMULATED_SWITCH),
                    (DiscoveryMethod) getArguments().getSerializable(DISCOVERY_METHOD),
                    discoveryMethods);
        } else {
            final FragmentActivity activity = getActivity();
            final boolean isSimulated;
            final int discoveryMethod;
            if (activity != null) {
                final SharedPreferences prefs = activity.getSharedPreferences(TAG, Context.MODE_PRIVATE);
                if (prefs != null) {
                    isSimulated = prefs.getBoolean(SIMULATED_SWITCH, false);
                    discoveryMethod = prefs.getInt(DISCOVERY_METHOD, 0);
                } else {
                    isSimulated = false;
                    discoveryMethod = 0;
                }
            } else {
                isSimulated = false;
                discoveryMethod = 0;
            }
            viewModel = new TerminalViewModel(isSimulated, discoveryMethods.get(discoveryMethod), discoveryMethods);
        }
    }

    @Override
    public @Nullable View onCreateView(
            @NotNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        final FragmentTerminalBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_terminal, container, false);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.discover_button).setOnClickListener(v -> {
            final FragmentActivity activity = getActivity();
            if (activity instanceof NavigationListener) {
                ((NavigationListener) activity).onRequestDiscovery(viewModel.simulated.getValue(), viewModel.getDiscoveryMethod());
            }
        });

        ((Spinner) view.findViewById(R.id.discovery_method_spinner)).setAdapter(
                new ArrayAdapter<DiscoveryMethod>(getContext(), android.R.layout.simple_spinner_item, discoveryMethods)
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            final SharedPreferences prefs = activity.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            if (prefs != null) {
                prefs.edit().putBoolean(SIMULATED_SWITCH, viewModel.simulated.getValue()).apply();
                prefs.edit().putInt(DISCOVERY_METHOD, viewModel.discoveryMethodPosition.getValue()).apply();
            }
        }
    }
}

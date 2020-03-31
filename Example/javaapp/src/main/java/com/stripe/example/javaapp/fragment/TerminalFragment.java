package com.stripe.example.javaapp.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.stripe.example.javaapp.NavigationListener;
import com.stripe.example.javaapp.R;
import com.stripe.example.javaapp.databinding.FragmentTerminalBinding;
import com.stripe.example.javaapp.viewmodel.TerminalViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The `TerminalFragment` is the main [Fragment] shown in the app, and handles navigation to any
 * other [Fragment]s as necessary.
 */
public class TerminalFragment extends Fragment {

    public static final String TAG = "com.stripe.example.fragment.TerminalFragment";
    private static final String SIMULATED_SWITCH = "simulated_switch";

    private TerminalViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            viewModel = new TerminalViewModel(getArguments().getBoolean(SIMULATED_SWITCH));
        } else {
            final FragmentActivity activity = getActivity();
            final boolean isSimulated;
            if (activity != null) {
                final SharedPreferences prefs = activity.getSharedPreferences(TAG, Context.MODE_PRIVATE);
                if (prefs != null) {
                    isSimulated = prefs.getBoolean(SIMULATED_SWITCH, false);
                } else {
                    isSimulated = false;
                }
            } else {
                isSimulated = false;
            }
            viewModel = new TerminalViewModel(isSimulated);
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
                ((NavigationListener) activity).onRequestDiscovery(viewModel.simulated.getValue());
            }
        });

        view.findViewById(R.id.simulated_switch).setOnClickListener(v -> {
            viewModel.simulated.setValue(!viewModel.simulated.getValue());
        });

        // TODO: Do this dynamically from the type selected
        ((TextView) view.findViewById(R.id.device_type_button)).setText(R.string.chipper_2x);
    }

    @Override
    public void onPause() {
        super.onPause();
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            final SharedPreferences prefs = activity.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            if (prefs != null) {
                prefs.edit().putBoolean(SIMULATED_SWITCH, viewModel.simulated.getValue()).apply();
            }
        }
    }
}

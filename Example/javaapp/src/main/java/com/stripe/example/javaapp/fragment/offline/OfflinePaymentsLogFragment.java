package com.stripe.example.javaapp.fragment.offline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stripe.example.javaapp.MainActivity;
import com.stripe.example.javaapp.NavigationListener;
import com.stripe.example.javaapp.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OfflinePaymentsLogFragment extends Fragment {
    @NotNull
    public static final String TAG = "com.stripe.example.fragment.OfflinePaymentsLogFragment";
    private OfflinePaymentsLogAdapter adapter;
    @Override
    @Nullable
    public View onCreateView(
            @NotNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_offline_payments_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.done_button).setOnClickListener(v -> {
            FragmentActivity activity = getActivity();
            if (activity instanceof NavigationListener) {
                ((MainActivity) activity).onRequestExitWorkflow();
            }
        });

        final RecyclerView logRecyclerView = view.findViewById(R.id.offline_logs_recycler_view);
        logRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OfflinePaymentsLogAdapter();
        logRecyclerView.setAdapter(adapter);

        FragmentActivity activity = requireActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).offlineModeHandler.liveLogs.observe(getViewLifecycleOwner(),
                logs -> adapter.updateOfflineLogs(logs)
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        FragmentActivity activity = requireActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).offlineModeHandler.liveLogs.removeObservers(getViewLifecycleOwner());
        }
    }
}



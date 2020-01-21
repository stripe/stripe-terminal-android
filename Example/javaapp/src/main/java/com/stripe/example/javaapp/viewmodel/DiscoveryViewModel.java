package com.stripe.example.javaapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.stripe.example.javaapp.fragment.discovery.ReaderClickListener;
import com.stripe.stripeterminal.callable.Cancelable;
import com.stripe.stripeterminal.model.external.Reader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryViewModel extends ViewModel {
    public final MutableLiveData<List<? extends Reader>> readers;
    public final MutableLiveData<Boolean> isConnecting;
    @Nullable public Cancelable discoveryTask;
    @Nullable public ReaderClickListener readerClickListener;

    public DiscoveryViewModel() {
        this(new ArrayList<>());
    }

    public DiscoveryViewModel(@NotNull List<Reader> readersParam) {
        readers = new MutableLiveData<>(readersParam);
        isConnecting = new MutableLiveData<>(false);
        discoveryTask = null;
        readerClickListener = null;
    }
}

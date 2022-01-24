package com.stripe.example.javaapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.stripe.stripeterminal.external.models.DiscoveryMethod;

public class TerminalViewModel extends ViewModel {
    public final MutableLiveData<Boolean> simulated;
    public final MutableLiveData<Integer> discoveryMethodPosition;
    private DiscoveryMethod discoveryMethod;


    public TerminalViewModel(boolean simulated, DiscoveryMethod discoveryMethod) {
        this.discoveryMethod = discoveryMethod;
        this.simulated = new MutableLiveData<>(simulated);
        this.discoveryMethodPosition = new MutableLiveData<>(discoveryMethod.ordinal());
    }

    public void setDiscoveryMethod(DiscoveryMethod discoveryMethod) {
        this.discoveryMethod = discoveryMethod;
        discoveryMethodPosition.setValue(discoveryMethod.ordinal());
    }

    public DiscoveryMethod getDiscoveryMethod() {
        return DiscoveryMethod.values()[discoveryMethodPosition.getValue()];
    }
}

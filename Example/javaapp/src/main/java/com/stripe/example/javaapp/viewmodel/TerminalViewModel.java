package com.stripe.example.javaapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.stripe.stripeterminal.external.models.DiscoveryMethod;

import java.util.List;

public class TerminalViewModel extends ViewModel {
    public final MutableLiveData<Boolean> simulated;
    public final MutableLiveData<Integer> discoveryMethodPosition;
    private List<DiscoveryMethod> discoveryMethods;
    private DiscoveryMethod discoveryMethod;


    public TerminalViewModel(boolean simulated, DiscoveryMethod discoveryMethod, List<DiscoveryMethod> discoveryMethods) {
        this.discoveryMethod = discoveryMethod;
        this.simulated = new MutableLiveData<>(simulated);
        this.discoveryMethods = discoveryMethods;
        this.discoveryMethodPosition = new MutableLiveData<>(discoveryMethods.indexOf(discoveryMethod));
    }

    public DiscoveryMethod getDiscoveryMethod() {
        return discoveryMethods.get(discoveryMethodPosition.getValue());
    }
}

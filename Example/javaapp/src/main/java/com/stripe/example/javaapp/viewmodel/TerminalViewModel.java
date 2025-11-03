package com.stripe.example.javaapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.stripe.example.javaapp.fragment.discovery.DiscoveryMethod;

import java.util.List;

public class TerminalViewModel extends ViewModel {
    public final MutableLiveData<Boolean> simulated;
    public final MutableLiveData<Integer> discoveryMethodPosition;
    public final MutableLiveData<Boolean> easyConnectSupported;
    public final MutableLiveData<Boolean> isConnecting;
    private List<DiscoveryMethod> discoveryMethods;
    private DiscoveryMethod discoveryMethod;


    public TerminalViewModel(boolean simulated, DiscoveryMethod discoveryMethod, List<DiscoveryMethod> discoveryMethods) {
        this.discoveryMethod = discoveryMethod;
        this.simulated = new MutableLiveData<>(simulated);
        this.discoveryMethods = discoveryMethods;
        this.discoveryMethodPosition = new MutableLiveData<>(discoveryMethods.indexOf(discoveryMethod));
        this.easyConnectSupported = new MutableLiveData<>(isEasyConnectSupported(discoveryMethod));
        this.isConnecting = new MutableLiveData<>(false);
        
        // Update easyConnectSupported when discovery method changes
        this.discoveryMethodPosition.observeForever(position -> {
            DiscoveryMethod method = discoveryMethods.get(position);
            easyConnectSupported.setValue(isEasyConnectSupported(method));
        });
    }

    public DiscoveryMethod getDiscoveryMethod() {
        return discoveryMethods.get(discoveryMethodPosition.getValue());
    }
    
    private boolean isEasyConnectSupported(DiscoveryMethod method) {
        return method == DiscoveryMethod.TAP_TO_PAY ||
               method == DiscoveryMethod.INTERNET;
    }
}

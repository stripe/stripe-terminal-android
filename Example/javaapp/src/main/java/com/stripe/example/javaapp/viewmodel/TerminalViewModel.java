package com.stripe.example.javaapp.viewmodel;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

public class TerminalViewModel extends BaseObservable {
    private boolean simulated;

    public TerminalViewModel() {
        this(false);
    }

    public TerminalViewModel(boolean simulated) {
        this.simulated = simulated;
    }

    @Bindable
    public Boolean getSimulated() {
        return simulated;
    }

    public void setSimulated(Boolean value) {
        if (simulated != value) {
            simulated = value;
            notifyPropertyChanged(BR.simulated);
        }
    }
}

package com.stripe.example.viewmodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class TerminalViewModel(private var _simulated: Boolean = false) : BaseObservable() {

    var simulated: Boolean
        @Bindable get() = _simulated
        set(value) {
            if (_simulated != value) {
                _simulated = value
                notifyPropertyChanged(BR.simulated)
            }
        }
}

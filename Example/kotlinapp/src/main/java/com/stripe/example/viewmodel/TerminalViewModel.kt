package com.stripe.example.viewmodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import com.stripe.stripeterminal.external.models.DiscoveryMethod

class TerminalViewModel(
    var discoveryMethod: DiscoveryMethod,
    var discoveryMethods: List<DiscoveryMethod>,
    private var _simulated: Boolean = false
) : BaseObservable() {

    var discoveryMethodPosition: Int
        @Bindable get() = discoveryMethods.indexOf(discoveryMethod)
        @Bindable set(value) {
            val newType = discoveryMethods[value]
            if (discoveryMethod != newType) {
                discoveryMethod = newType
                notifyPropertyChanged(BR.discoveryMethodPosition)
            }
        }
    var simulated: Boolean
        @Bindable get() = _simulated
        set(value) {
            if (_simulated != value) {
                _simulated = value
                notifyPropertyChanged(BR.simulated)
            }
        }
}

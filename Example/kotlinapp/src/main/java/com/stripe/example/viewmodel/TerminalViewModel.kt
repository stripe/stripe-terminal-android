package com.stripe.example.viewmodel

import com.stripe.example.fragment.discovery.DiscoveryMethod

class TerminalViewModel(
    var discoveryMethod: DiscoveryMethod,
    var simulated: Boolean = false
) {
    var discoveryMethods: List<DiscoveryMethod> = DiscoveryMethod.entries

    var discoveryMethodPosition: Int
        get() = discoveryMethods.indexOf(discoveryMethod)
        set(value) {
            val newType = discoveryMethods[value]
            if (discoveryMethod != newType) {
                discoveryMethod = newType
            }
        }
}

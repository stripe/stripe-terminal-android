package com.stripe.example.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stripe.example.NavigationListener
import com.stripe.example.fragment.discovery.ReaderClickListener
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader

class DiscoveryViewModel constructor(readersParam: List<Reader> = listOf()) : ViewModel() {
    val readers = MutableLiveData(readersParam)
    var isConnecting: MutableLiveData<Boolean> = MutableLiveData(false)
    var isUpdating: MutableLiveData<Boolean> = MutableLiveData(false)
    var updateProgress: MutableLiveData<Float> = MutableLiveData(0F)
    var discoveryTask: Cancelable? = null
    var readerClickListener: ReaderClickListener? = null
    var navigationListener: NavigationListener? = null
    val selectedLocation = MutableLiveData<Location?>(null)
}

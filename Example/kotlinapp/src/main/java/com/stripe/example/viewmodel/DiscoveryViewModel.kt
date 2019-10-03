package com.stripe.example.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stripe.example.fragment.discovery.ReaderClickListener
import com.stripe.stripeterminal.callable.Cancelable
import com.stripe.stripeterminal.model.external.Reader

class DiscoveryViewModel constructor(readersParam: List<Reader> = listOf()) : ViewModel() {

    val readers = MutableLiveData(readersParam)
    var isConnecting: MutableLiveData<Boolean> = MutableLiveData(false)
    var discoveryTask: Cancelable? = null
    var readerClickListener: ReaderClickListener? = null
}

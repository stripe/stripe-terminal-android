package com.stripe.example.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stripe.example.model.Event
import com.stripe.stripeterminal.callable.Cancelable

class EventViewModel constructor(eventsList: List<Event> = mutableListOf()) : ViewModel() {
    private val eventList: ArrayList<Event> = ArrayList(eventsList)
    var events: MutableLiveData<List<Event>> = MutableLiveData(eventList)
    var isComplete: MutableLiveData<Boolean> = MutableLiveData(false)
    var collectTask: Cancelable? = null

    fun addEvent(event: Event) {
        eventList.add(event)
        events.value = eventList
    }
}

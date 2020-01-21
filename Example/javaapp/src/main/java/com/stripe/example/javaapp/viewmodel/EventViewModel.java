package com.stripe.example.javaapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.stripe.example.javaapp.model.Event;
import com.stripe.stripeterminal.callable.Cancelable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EventViewModel extends ViewModel {
    @NotNull private List<Event> eventList;
    @NotNull public final MutableLiveData<List<Event>> events;
    @NotNull public final MutableLiveData<Boolean> isComplete;
    @Nullable public Cancelable collectTask;

    public EventViewModel() {
        this(new ArrayList<>());
    }

    public EventViewModel(@NotNull List<Event> eventList) {
        this.eventList = eventList;
        events = new MutableLiveData<>(eventList);
        isComplete = new MutableLiveData<>(false);
        collectTask = null;
    }

    public void addEvent(Event event) {
        eventList.add(event);
        events.setValue(eventList);
    }
}

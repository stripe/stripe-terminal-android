package com.stripe.example.javaapp;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * Wrapper for listeners to allow multiple listener registrations
 * @param <T> any listener
 */
public class ListenerAnnouncer<T> {
    private final HashSet<WeakReference<T>> listeners = new HashSet<>();

    public void addListener(T listener) {
        listeners.add(new WeakReference<>(listener));
    }

    public void removeListener(T listener) {
        listeners.removeIf(tWeakReference -> {
            T ref = tWeakReference.get();
            return ref == listener;
        });
    }

    protected void announce(Consumer<T> fn) {
        listeners.forEach(tWeakReference -> {
            T listener = tWeakReference.get();
            if (listener != null) {
                fn.accept(listener);
            }
        });
    }
}

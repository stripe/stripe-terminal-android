package com.stripe.example;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * The {@code ReaderList} is a simple class that will statically hold any readers that have been
 * found.
 */
public class ReaderList {

    private static List<ReaderSelectionListener> listeners = Lists.newArrayList();
    private static String[] readers = new String[0];

    /**
     * Register a listener that will be informed whenever the nearby readers list is updated
     * @param listener The listener to register
     * @return The current set of readers
     */
    public static String[] registerListener(ReaderSelectionListener listener) {
        listeners.add(listener);
        return readers;
    }

    /**
     * Update the list of nearby readers and alert all listeners
     * @param newReaders the list of readers
     */
    public static void update(String[] newReaders) {
        readers = newReaders;
        for (ReaderSelectionListener listener : listeners) {
            listener.updateReaders(readers);
        }
    }

    /**
     * A simple listener interface that classes can implement to be notified of updates to the
     * reader list.
     */
    public interface ReaderSelectionListener {
        void updateReaders(String[] readers);
    }
}

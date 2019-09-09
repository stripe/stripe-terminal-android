package com.stripe.example.javaapp.model;

import org.jetbrains.annotations.NotNull;

public class Event {
    @NotNull private final String mMessage;
    @NotNull private final String mMethod;

    public Event(@NotNull String message, @NotNull String method) {
        mMessage = message;
        mMethod = method;
    }

    @NotNull
    public String getMessage() {
        return mMessage;
    }

    @NotNull
    public String getMethod() {
        return mMethod;
    }
}

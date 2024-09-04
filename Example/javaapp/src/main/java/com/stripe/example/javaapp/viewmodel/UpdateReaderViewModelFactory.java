package com.stripe.example.javaapp.viewmodel;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import org.jetbrains.annotations.NotNull;

public class UpdateReaderViewModelFactory implements ViewModelProvider.Factory {
    @NotNull
    private final Resources resources;

    public UpdateReaderViewModelFactory(@NotNull Resources resources) {
        this.resources = resources;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
        return (T) new UpdateReaderViewModel(resources);
    }
}

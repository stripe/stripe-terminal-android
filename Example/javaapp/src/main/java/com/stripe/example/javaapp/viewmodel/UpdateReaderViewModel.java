package com.stripe.example.javaapp.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.stripe.example.javaapp.R;
import com.stripe.stripeterminal.callable.Cancelable;
import com.stripe.stripeterminal.model.external.Reader;
import com.stripe.stripeterminal.model.external.ReaderSoftwareUpdate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UpdateReaderViewModel extends AndroidViewModel {
    @NotNull public MutableLiveData<Double> progress;
    @NotNull public MutableLiveData<Boolean> hasStartedFetchingUpdate;
    @NotNull public MutableLiveData<Boolean> hasFinishedFetchingUpdate;
    @NotNull public MutableLiveData<Boolean> hasStartedInstallingUpdate;
    @NotNull public MutableLiveData<Boolean> hasFinishedInstallingUpdate;
    @NotNull public MutableLiveData<@Nullable ReaderSoftwareUpdate> readerSoftwareUpdate;

    @NotNull public MediatorLiveData<Boolean> checkForUpdateDescriptionVisibility = new MediatorLiveData<>();
    @NotNull public MediatorLiveData<Boolean> checkForUpdateButtonVisibility = new MediatorLiveData<>();
    @NotNull public MediatorLiveData<Integer> checkForUpdateButtonColor = new MediatorLiveData<>();
    @NotNull public MediatorLiveData<Integer> checkForUpdateButtonText = new MediatorLiveData<>();
    @NotNull public MediatorLiveData<String> checkForUpdateDescriptionText = new MediatorLiveData<>();
    @NotNull public MediatorLiveData<Boolean> doneButtonVisibility = new MediatorLiveData<>();
    @NotNull public MediatorLiveData<Boolean> installDisclaimerVisibility = new MediatorLiveData<>();

    @Nullable public Cancelable fetchUpdateOperation = null;
    @Nullable public Cancelable installOperation = null;
    @Nullable public Reader reader = null;

    public UpdateReaderViewModel(@NotNull Application app) {
        super(app);

        progress = new MutableLiveData<>(0.0);
        hasStartedFetchingUpdate = new MutableLiveData<>(false);
        hasFinishedFetchingUpdate = new MutableLiveData<>(false);
        hasStartedInstallingUpdate = new MutableLiveData<>(false);
        hasFinishedInstallingUpdate = new MutableLiveData<>(false);
        readerSoftwareUpdate = new MutableLiveData<>(null);

        final Observer<Object> updateDescriptionVisibilityLambda = value ->
                checkForUpdateDescriptionVisibility.setValue(getCheckForUpdateDescriptionVisibility());
        checkForUpdateDescriptionVisibility.addSource(hasStartedFetchingUpdate, updateDescriptionVisibilityLambda);
        checkForUpdateDescriptionVisibility.addSource(hasFinishedFetchingUpdate, updateDescriptionVisibilityLambda);
        checkForUpdateDescriptionVisibility.addSource(hasStartedInstallingUpdate, updateDescriptionVisibilityLambda);
        checkForUpdateDescriptionVisibility.addSource(readerSoftwareUpdate, updateDescriptionVisibilityLambda);

        final Observer<Object> checkForUpdateButtonVisibilityLambda = value ->
                checkForUpdateButtonVisibility.setValue(getCheckForUpdateButtonVisibility());
        checkForUpdateButtonVisibility.addSource(hasStartedInstallingUpdate, checkForUpdateButtonVisibilityLambda);
        checkForUpdateButtonVisibility.addSource(hasFinishedInstallingUpdate, checkForUpdateButtonVisibilityLambda);
        checkForUpdateButtonVisibility.addSource(hasStartedFetchingUpdate, checkForUpdateButtonVisibilityLambda);
        checkForUpdateButtonVisibility.addSource(hasFinishedFetchingUpdate, checkForUpdateButtonVisibilityLambda);
        checkForUpdateButtonVisibility.addSource(readerSoftwareUpdate, checkForUpdateButtonVisibilityLambda);

        final Observer<Boolean> checkForUpdateButtonColorLambda =
                value -> checkForUpdateButtonColor.setValue(getCheckForUpdateButtonColor());
        checkForUpdateButtonColor.addSource(hasStartedFetchingUpdate, checkForUpdateButtonColorLambda);
        checkForUpdateButtonColor.addSource(hasFinishedFetchingUpdate, checkForUpdateButtonColorLambda);

        final Observer<Object> checkForUpdateButtonTextLambda =
                value -> checkForUpdateButtonText.setValue(getCheckForUpdateButtonText());
        checkForUpdateButtonText.addSource(hasStartedFetchingUpdate, checkForUpdateButtonTextLambda);
        checkForUpdateButtonText.addSource(hasFinishedFetchingUpdate, checkForUpdateButtonTextLambda);
        checkForUpdateButtonText.addSource(hasStartedInstallingUpdate, checkForUpdateButtonTextLambda);
        checkForUpdateButtonText.addSource(readerSoftwareUpdate, checkForUpdateButtonTextLambda);

        final Observer<Object> checkForUpdateDescriptionTextLambda =
                value -> checkForUpdateDescriptionText.setValue(getCheckForUpdateDescriptionText());
        checkForUpdateDescriptionText.addSource(hasStartedFetchingUpdate, checkForUpdateDescriptionTextLambda);
        checkForUpdateDescriptionText.addSource(hasFinishedFetchingUpdate, checkForUpdateDescriptionTextLambda);
        checkForUpdateDescriptionText.addSource(hasStartedInstallingUpdate, checkForUpdateDescriptionTextLambda);
        checkForUpdateDescriptionText.addSource(hasFinishedInstallingUpdate, checkForUpdateDescriptionTextLambda);
        checkForUpdateDescriptionText.addSource(progress, checkForUpdateDescriptionTextLambda);

        final Observer<Object> doneButtonVisibilityLambda =
                value -> doneButtonVisibility.setValue(getDoneButtonVisibility());
        doneButtonVisibility.addSource(hasFinishedInstallingUpdate, doneButtonVisibilityLambda);
        doneButtonVisibility.addSource(hasStartedFetchingUpdate, doneButtonVisibilityLambda);
        doneButtonVisibility.addSource(hasFinishedFetchingUpdate, doneButtonVisibilityLambda);
        doneButtonVisibility.addSource(hasStartedInstallingUpdate, doneButtonVisibilityLambda);
        doneButtonVisibility.addSource(readerSoftwareUpdate, doneButtonVisibilityLambda);

        final Observer<Object> installDisclaimerVisibilityLambda =
                value -> installDisclaimerVisibility.setValue(getInstallDisclaimerVisiblity());
        installDisclaimerVisibility.addSource(hasStartedInstallingUpdate, installDisclaimerVisibilityLambda);
        installDisclaimerVisibility.addSource(hasFinishedFetchingUpdate, installDisclaimerVisibilityLambda);
    }

    private boolean getCheckForUpdateDescriptionVisibility() {
        return (hasStartedFetchingUpdate.getValue() && !hasFinishedFetchingUpdate.getValue()) ||
                (hasStartedFetchingUpdate.getValue() && hasFinishedFetchingUpdate.getValue() &&
                !hasStartedInstallingUpdate.getValue() && readerSoftwareUpdate.getValue() != null) ||
                hasStartedInstallingUpdate.getValue();
    }

    private boolean getCheckForUpdateButtonVisibility() {
        return !(hasStartedInstallingUpdate.getValue() && hasFinishedInstallingUpdate.getValue()) ||
                (hasStartedFetchingUpdate.getValue() && hasFinishedFetchingUpdate.getValue() &&
                !hasStartedInstallingUpdate.getValue() && readerSoftwareUpdate.getValue() != null);
    }

    private int getCheckForUpdateButtonColor() {
        if (hasStartedFetchingUpdate.getValue() && !hasFinishedFetchingUpdate.getValue()) {
            return R.color.colorPrimaryDark;
        } else {
            return R.color.colorAccent;
        }
    }

    private int getCheckForUpdateButtonText() {
        if (hasStartedFetchingUpdate.getValue() && !hasFinishedFetchingUpdate.getValue()) {
            return R.string.checking_for_update;
        } else if (hasStartedFetchingUpdate.getValue() && hasFinishedFetchingUpdate.getValue() &&
                !hasStartedInstallingUpdate.getValue()) {
            return readerSoftwareUpdate.getValue() != null ?
                    R.string.install_update : R.string.no_update_available;
        } else if (hasStartedInstallingUpdate.getValue()) {
            return R.string.update_in_progress;
        } else {
            return R.string.check_for_update;
        }
    }

    @NotNull
    private String getCheckForUpdateDescriptionText() {
        final Context context = getApplication().getApplicationContext();
        if (hasStartedFetchingUpdate.getValue() && !hasFinishedFetchingUpdate.getValue()) {
            return context.getString(R.string.checking_for_update);
        } else if (hasStartedFetchingUpdate.getValue() && hasFinishedFetchingUpdate.getValue() &&
                !hasStartedInstallingUpdate.getValue() && readerSoftwareUpdate.getValue() != null) {
            return context.getString(R.string.install_explanation,
                    readerSoftwareUpdate.getValue().getVersion(),
                    readerSoftwareUpdate.getValue().getTimeEstimate().getDescription());
        } else if (hasStartedInstallingUpdate.getValue()) {
            return hasFinishedInstallingUpdate.getValue() ? context.getString(R.string.update_complete) :
                context.getString(R.string.update_progress, Double.toString(
                        Math.round((progress.getValue() != null ? progress.getValue() : 0.0) * 100)));
        } else {
            return context.getString(R.string.update_explanation);
        }
    }

    private boolean getDoneButtonVisibility() {
        return hasFinishedInstallingUpdate.getValue() || (hasStartedFetchingUpdate.getValue() &&
                hasFinishedFetchingUpdate.getValue() && !hasStartedInstallingUpdate.getValue() &&
                readerSoftwareUpdate.getValue() == null);
    }

    private boolean getInstallDisclaimerVisiblity() {
        return hasStartedInstallingUpdate.getValue() && hasFinishedFetchingUpdate.getValue();
    }
}

package com.stripe.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.stripe.example.R
import com.stripe.example.StripeTerminalApplication
import com.stripe.stripeterminal.callable.Cancelable
import com.stripe.stripeterminal.model.external.Reader
import com.stripe.stripeterminal.model.external.ReaderSoftwareUpdate
import kotlin.math.roundToInt

class UpdateReaderViewModel
constructor(application: Application) : AndroidViewModel(application) {
    var progress: MutableLiveData<Double> = MutableLiveData(0.0)
    var hasStartedFetchingUpdate: MutableLiveData<Boolean> = MutableLiveData(false)
    var hasFinishedFetchingUpdate: MutableLiveData<Boolean> = MutableLiveData(false)
    var hasStartedInstallingUpdate: MutableLiveData<Boolean> = MutableLiveData(false)
    var hasFinishedInstallingUpdate: MutableLiveData<Boolean> = MutableLiveData(false)
    var readerSoftwareUpdate: MutableLiveData<ReaderSoftwareUpdate?> = MutableLiveData(null)
    var fetchUpdateOperation: Cancelable? = null
    var installOperation: Cancelable? = null
    var reader: Reader? = null

    val checkForUpdateDescriptionVisibility: MediatorLiveData<Boolean> = MediatorLiveData()
    val checkForUpdateButtonVisibility: MediatorLiveData<Boolean> = MediatorLiveData()
    val checkForUpdateButtonColor: MediatorLiveData<Int> = MediatorLiveData()
    val checkForUpdateButtonText: MediatorLiveData<Int> = MediatorLiveData()
    val checkForUpdateDescriptionText: MediatorLiveData<String> = MediatorLiveData()
    val doneButtonVisibility: MediatorLiveData<Boolean> = MediatorLiveData()

    init {
        val updateDescriptionVisibilityLambda: Observer<Boolean> = Observer {
            checkForUpdateDescriptionVisibility.value = getCheckForUpdateDescriptionVisibility()
        }
        checkForUpdateDescriptionVisibility
                .addSource(hasStartedFetchingUpdate, updateDescriptionVisibilityLambda)
        checkForUpdateDescriptionVisibility
                .addSource(hasFinishedFetchingUpdate, updateDescriptionVisibilityLambda)
        checkForUpdateDescriptionVisibility
                .addSource(hasStartedInstallingUpdate, updateDescriptionVisibilityLambda)
        checkForUpdateDescriptionVisibility.addSource(readerSoftwareUpdate) {
            checkForUpdateDescriptionVisibility.value = getCheckForUpdateDescriptionVisibility()
        }

        val checkForUpdateButtonVisibilityLambda: Observer<Boolean> = Observer {
            checkForUpdateButtonVisibility.value = getCheckForUpdateButtonVisibility()
        }
        checkForUpdateButtonVisibility
                .addSource(hasStartedInstallingUpdate, checkForUpdateButtonVisibilityLambda)
        checkForUpdateButtonVisibility
                .addSource(hasFinishedInstallingUpdate, checkForUpdateButtonVisibilityLambda)
        checkForUpdateButtonVisibility
                .addSource(hasStartedFetchingUpdate, checkForUpdateButtonVisibilityLambda)
        checkForUpdateButtonVisibility
                .addSource(hasFinishedFetchingUpdate, checkForUpdateButtonVisibilityLambda)
        checkForUpdateButtonVisibility.addSource(readerSoftwareUpdate) {
            checkForUpdateButtonVisibility.value = getCheckForUpdateButtonVisibility()
        }

        val checkForUpdateButtonColorLambda = Observer<Boolean> {
            checkForUpdateButtonColor.value = getCheckForUpdateButtonColor()
        }
        checkForUpdateButtonColor
                .addSource(hasStartedFetchingUpdate, checkForUpdateButtonColorLambda)
        checkForUpdateButtonColor
                .addSource(hasFinishedFetchingUpdate, checkForUpdateButtonColorLambda)

        val checkForUpdateButtonTextLambda = Observer<Boolean> {
            checkForUpdateButtonText.value = getCheckForUpdateButtonText()
        }
        checkForUpdateButtonText
                .addSource(hasStartedFetchingUpdate, checkForUpdateButtonTextLambda)
        checkForUpdateButtonText
                .addSource(hasFinishedFetchingUpdate, checkForUpdateButtonTextLambda)
        checkForUpdateButtonText
                .addSource(hasStartedInstallingUpdate, checkForUpdateButtonTextLambda)
        checkForUpdateButtonText.addSource(readerSoftwareUpdate) {
            checkForUpdateButtonText.value = getCheckForUpdateButtonText()
        }

        val checkForUpdateDescriptionTextLambda = Observer<Any> {
            checkForUpdateDescriptionText.value = getCheckForUpdateDescriptionText()
        }
        checkForUpdateDescriptionText.addSource(hasStartedFetchingUpdate, checkForUpdateDescriptionTextLambda)
        checkForUpdateDescriptionText.addSource(hasFinishedFetchingUpdate, checkForUpdateDescriptionTextLambda)
        checkForUpdateDescriptionText.addSource(hasStartedInstallingUpdate, checkForUpdateDescriptionTextLambda)
        checkForUpdateDescriptionText.addSource(hasFinishedInstallingUpdate, checkForUpdateDescriptionTextLambda)
        checkForUpdateDescriptionText.addSource(progress, checkForUpdateDescriptionTextLambda)

        val doneButtonVisibilityLambda = Observer<Boolean> {
            doneButtonVisibility.value = getDoneButtonVisibility()
        }
        doneButtonVisibility.addSource(hasFinishedInstallingUpdate, doneButtonVisibilityLambda)
        doneButtonVisibility.addSource(hasStartedFetchingUpdate, doneButtonVisibilityLambda)
        doneButtonVisibility.addSource(hasFinishedFetchingUpdate, doneButtonVisibilityLambda)
        doneButtonVisibility.addSource(hasStartedInstallingUpdate, doneButtonVisibilityLambda)
        doneButtonVisibility.addSource(readerSoftwareUpdate) {
            doneButtonVisibility.value = getDoneButtonVisibility()
        }
    }

    private fun getCheckForUpdateDescriptionVisibility(): Boolean {
        return (hasStartedFetchingUpdate.value!! && !hasFinishedFetchingUpdate.value!!) ||
                (hasStartedFetchingUpdate.value!! && hasFinishedFetchingUpdate.value!! &&
                !hasStartedInstallingUpdate.value!! && readerSoftwareUpdate.value != null) ||
                hasStartedInstallingUpdate.value!!
    }

    private fun getCheckForUpdateButtonVisibility(): Boolean {
        return !(hasStartedInstallingUpdate.value!! && hasFinishedInstallingUpdate.value!!) ||
                (hasStartedFetchingUpdate.value!! && hasFinishedFetchingUpdate.value!! &&
                        !hasStartedInstallingUpdate.value!! && readerSoftwareUpdate.value != null)
    }

    private fun getCheckForUpdateButtonColor(): Int {
        return if (hasStartedFetchingUpdate.value!! && !hasFinishedFetchingUpdate.value!!) {
            R.color.colorPrimaryDark
        } else {
            R.color.colorAccent
        }
    }

    private fun getCheckForUpdateButtonText(): Int {
        return if (hasStartedFetchingUpdate.value!! && !hasFinishedFetchingUpdate.value!!) {
            R.string.checking_for_update
        } else if (hasStartedFetchingUpdate.value!! && hasFinishedFetchingUpdate.value!! &&
                !hasStartedInstallingUpdate.value!!) {
            if (readerSoftwareUpdate.value != null) R.string.install_update else R.string.no_update_available
        } else if (hasStartedInstallingUpdate.value!!) {
            R.string.update_in_progress
        } else {
            R.string.check_for_update
        }
    }

    private fun getCheckForUpdateDescriptionText(): String {
        val context = getApplication<StripeTerminalApplication>().applicationContext
        return if (hasStartedFetchingUpdate.value!! && !hasFinishedFetchingUpdate.value!!) {
            context.getString(R.string.checking_for_update)
        } else if (hasStartedFetchingUpdate.value!! && hasFinishedFetchingUpdate.value!! &&
                !hasStartedInstallingUpdate.value!! && readerSoftwareUpdate.value != null) {
            context.getString(R.string.install_explanation,
                    readerSoftwareUpdate.value!!.version,
                    readerSoftwareUpdate.value!!.timeEstimate.description)
        } else if (hasStartedInstallingUpdate.value!!) {
            if (hasFinishedInstallingUpdate.value!!) context.getString(R.string.update_complete)
            else context.getString(R.string.update_progress, (((progress.value ?: 0.0) * 100).roundToInt()).toString())
        } else {
            context.getString(R.string.update_explanation)
        }
    }

    private fun getDoneButtonVisibility(): Boolean {
        return hasFinishedInstallingUpdate.value!! || (hasStartedFetchingUpdate.value!! &&
                hasFinishedFetchingUpdate.value!! && !hasStartedInstallingUpdate.value!! &&
                readerSoftwareUpdate.value == null)
    }
}

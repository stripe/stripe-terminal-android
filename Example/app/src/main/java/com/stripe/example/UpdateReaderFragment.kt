package com.stripe.example


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.stripe.stripeterminal.*
import kotlinx.android.synthetic.main.fragment_update_reader.view.*

/**
 * The `UpdateReaderFragment` allows the user to check the current version of the [Reader] software,
 * as well as update it when necessary.
 */
class UpdateReaderFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_update_reader, container, false)

        // Set the description of the connected reader
        Terminal.getInstance().connectedReader?.let {
            view.reader_description.text = getString(R.string.reader_description,
                    it.deviceType.name, it.serialNumber)
            view.current_version.text = it.softwareVersion
        }

        // Rig up the cancel button
        view.cancel_button.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onRequestExitWorkflow()
            }
        }

        // Start the update action
        view.check_for_update_button.setOnClickListener {
            view.check_for_update_button.text = getString(R.string.checking_for_update)
            view.check_for_update_button.setTextColor(ContextCompat.getColor(context!!,
                    R.color.colorPrimaryDark))
            view.check_for_update_description.text = getString(R.string.checking_for_update)
            if (activity is NavigationListener) {
                (activity as NavigationListener).onRequestCheckForUpdate()
            }
        }

        return view
    }

    fun onCompleteUpdate() {
       showDoneButton()
        view?.check_for_update_button?.visibility = View.INVISIBLE
        view?.check_for_update_description?.text = getString(R.string.update_complete)
        view?.install_disclaimer?.visibility = View.INVISIBLE
    }

    fun onUpdateAvailable(update: ReaderSoftwareUpdate?) {
        if (update != null) {
            view?.check_for_update_button?.setOnClickListener {
                if (activity is NavigationListener) {
                    (activity as NavigationListener).onRequestInstallUpdate()
                }
                view?.check_for_update_button?.text = getString(R.string.update_in_progress)
                view?.check_for_update_description?.text = getString(R.string.update_progress, "0")
                view?.install_disclaimer?.visibility = View.VISIBLE
            }
            view?.check_for_update_button?.text = getString(R.string.install_update)
            view?.check_for_update_button?.setTextColor(ContextCompat.getColor(context!!,
                    R.color.colorAccent))
            view?.check_for_update_description?.text = getString(R.string.install_explanation,
                    update.version, update.timeEstimate.description)
        } else {
            view?.check_for_update_button?.text = getString(R.string.no_update_available)
            view?.check_for_update_button?.setTextColor(ContextCompat.getColor(context!!,
                    R.color.colorAccent))
            view?.check_for_update_description?.visibility = View.INVISIBLE
            showDoneButton()
        }
    }

    fun onUpdateProgress(progress: Float) {
        view?.check_for_update_description?.text = getString(R.string.update_progress,
                (progress * 100).toInt().toString())
    }

    private fun showDoneButton() {
        view?.cancel_button?.setTextColor(ContextCompat.getColor(context!!,
                R.color.colorPrimaryDark))
        view?.done_button?.visibility = View.VISIBLE
        view?.done_button?.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onRequestExitWorkflow()
            }
        }
    }
}

package com.stripe.example.fragment.location

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.FragmentLocationCreateBinding
import com.stripe.example.network.ApiClient

/**
 * Form input to create a new location on the API.
 */
class LocationCreateFragment : Fragment() {
    private var binding: FragmentLocationCreateBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location_create, container, false)
        binding = FragmentLocationCreateBinding.bind(view)

        binding?.locationCreateSubmit?.setOnClickListener {
            onSubmit()
        }

        return view
    }

    private fun onSubmit() {
        try {
            ApiClient.createLocation(
                displayName = binding?.locationCreateDisplayNameInput?.requiredValue,
                city = binding?.locationCreateAddressCityInput?.value,
                country = binding?.locationCreateAddressCountryInput?.requiredValue,
                line1 = binding?.locationCreateAddressLine1Input?.value,
                line2 = binding?.locationCreateAddressLine2Input?.value,
                postalCode = binding?.locationCreateAddressPostalInput?.value,
                state = binding?.locationCreateAddressStateInput?.value,
            )
            (activity as NavigationListener).onLocationCreated()
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Missing required input")
        } catch (e: Throwable) {
            view?.run { Snackbar.make(this, e.message ?: "Unknown Error", Snackbar.LENGTH_LONG).show() }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "CreateLocationFragment"

        fun newInstance() = LocationCreateFragment()
    }

    private val EditText.value: String? get() = if (text.isNullOrBlank()) null else text.toString()

    private val EditText.requiredValue: String get() {
        if (text.isNullOrBlank()) {
            error = resources.getString(R.string.field_required)
            throw IllegalStateException()
        }
        return text.toString()
    }
}

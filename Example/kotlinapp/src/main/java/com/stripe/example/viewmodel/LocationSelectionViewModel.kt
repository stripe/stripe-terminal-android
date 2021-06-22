package com.stripe.example.viewmodel

import androidx.lifecycle.ViewModel
import com.stripe.example.fragment.location.LocationListState
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.LocationListCallback
import com.stripe.stripeterminal.external.models.ListLocationsParameters
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.TerminalException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val LOCATION_QUERY_LIMIT = 100

class LocationSelectionViewModel : ViewModel() {
    private val mutableListState = MutableStateFlow(LocationListState())
    val listState: StateFlow<LocationListState> = mutableListState

    private val mutableError = MutableSharedFlow<TerminalException>()
    val error: Flow<TerminalException> = mutableError

    private val locationCallback = object : LocationListCallback {
        override fun onSuccess(locations: List<Location>, hasMore: Boolean) {
            mutableListState.value = mutableListState.value.let {
                it.copy(
                    locations = it.locations + locations,
                    hasMore = hasMore,
                    isLoading = false,
                )
            }
        }

        override fun onFailure(e: TerminalException) {
            mutableError.tryEmit(e)
        }
    }

    init {
        load()
    }

    fun loadMoreLocations() {
        if (listState.value.isLoading) return
        if (!listState.value.hasMore) return

        mutableListState.value = mutableListState.value.copy(isLoading = true)

        val parameters = ListLocationsParameters.Builder()
            .apply {
                startingAfter = mutableListState.value.locations.last().id
                limit = LOCATION_QUERY_LIMIT
            }
            .build()
        Terminal.getInstance().listLocations(parameters, locationCallback)
    }

    fun reload() {
        mutableListState.value = LocationListState()
        load()
    }

    private fun load() {
        Terminal.getInstance().listLocations(
            ListLocationsParameters.Builder().apply {
                limit = LOCATION_QUERY_LIMIT
            }.build(),
            locationCallback
        )
    }
}

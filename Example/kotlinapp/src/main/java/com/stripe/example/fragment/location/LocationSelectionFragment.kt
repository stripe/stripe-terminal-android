package com.stripe.example.fragment.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.stripe.example.MainActivity
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.FragmentLocationSelectionBinding
import com.stripe.example.fragment.launchAndRepeatWithViewLifecycle
import com.stripe.example.recyclerview.InfiniteScrollListener
import com.stripe.example.viewmodel.LocationSelectionViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * List of locations available to select as the reader connection location.
 */
class LocationSelectionFragment : Fragment() {
    private lateinit var viewModel: LocationSelectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[LocationSelectionViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        viewModel.reload()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val layoutManager = LinearLayoutManager(activity)
        val view = inflater.inflate(R.layout.fragment_location_selection, container, false)
        val binding = FragmentLocationSelectionBinding.bind(view)
        val adapter = LocationListAdapter(layoutInflater, activity as LocationSelectionController)

        binding.locationSelectionList.layoutManager = layoutManager
        binding.locationSelectionList.addOnScrollListener(
            InfiniteScrollListener(layoutManager, viewModel::loadMoreLocations)
        )
        binding.locationSelectionList.adapter = adapter
        binding.locationSelectionToolbar.inflateMenu(R.menu.location_selection)
        binding.locationSelectionToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_location_selection_add -> true.also {
                    (activity as NavigationListener).onRequestCreateLocation()
                }
                else -> false
            }
        }

        binding.locationSelectionCancelButton.setOnClickListener {
            (requireActivity() as MainActivity).onCancelLocationSelection()
        }

        launchAndRepeatWithViewLifecycle {
            viewModel.listState.collectLatest {
                adapter.locationListState = it
            }
        }

        launchAndRepeatWithViewLifecycle {
            viewModel.error.collect {
                Toast.makeText(activity, it.errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        return view
    }

    companion object {
        const val TAG = "LocationSelectionFragment"

        fun newInstance() = LocationSelectionFragment()
    }
}

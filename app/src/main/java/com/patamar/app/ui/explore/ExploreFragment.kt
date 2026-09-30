package com.patamar.app.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.patamar.app.R
import com.patamar.app.core.extensions.showSnackbar
import com.patamar.app.data.model.Event
import com.patamar.app.data.model.EventCategory
import com.patamar.app.databinding.FragmentExploreBinding
import com.patamar.app.ui.auth.AuthActivity
import com.patamar.app.ui.explore.adapter.EventListAdapter
import com.patamar.app.ui.explore.adapter.FeaturedEventAdapter
import com.patamar.app.ui.map.EventDetailBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by viewModels()

    private lateinit var featuredAdapter: FeaturedEventAdapter
    private lateinit var nearbyAdapter: EventListAdapter
    private lateinit var weekendAdapter: EventListAdapter
    private lateinit var freeAdapter: EventListAdapter
    private lateinit var searchAdapter: EventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupCategoryChips()
        setupSearch()
        observeState()
    }

    private fun setupAdapters() {
        featuredAdapter = FeaturedEventAdapter(onClick = ::showEventDetail, onSaveClick = ::onSaveClicked)
        binding.rvFeatured.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredAdapter
        }

        nearbyAdapter = EventListAdapter(onClick = ::showEventDetail)
        binding.rvNearby.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = nearbyAdapter
            isNestedScrollingEnabled = false
        }

        weekendAdapter = EventListAdapter(onClick = ::showEventDetail)
        binding.rvWeekend.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = weekendAdapter
            isNestedScrollingEnabled = false
        }

        freeAdapter = EventListAdapter(onClick = ::showEventDetail)
        binding.rvFree.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = freeAdapter
            isNestedScrollingEnabled = false
        }

        searchAdapter = EventListAdapter(onClick = ::showEventDetail)
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }
    }

    private fun showEventDetail(event: Event) {
        EventDetailBottomSheet.newInstance(event).show(childFragmentManager, "event_detail")
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategoryFilter.removeAllViews()
        val allChip = Chip(requireContext()).apply {
            text = "Todas"
            isCheckable = true
            isChecked = true
            setOnClickListener { viewModel.onCategorySelected(null) }
        }
        binding.chipGroupCategoryFilter.addView(allChip)
        EventCategory.values().forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category.label
                isCheckable = true
                setOnClickListener { viewModel.onCategorySelected(category) }
            }
            binding.chipGroupCategoryFilter.addView(chip)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.onSearchQueryChanged(text?.toString().orEmpty())
        }
    }

    private fun onSaveClicked(event: Event) {
        if (viewModel.canSave) {
            viewModel.toggleSaved(event)
        } else {
            binding.root.showSnackbar(getString(R.string.event_save_needs_account), "Entrar") {
                startActivity(AuthActivity.intent(requireContext()))
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.savedEventIds.collect { featuredAdapter.updateSaved(it) }
                }
                viewModel.uiModel.collect { model ->
                    val isSearching = model.searchResults != null
                    binding.groupBrowse.visibility = if (isSearching) View.GONE else View.VISIBLE
                    binding.rvSearchResults.visibility = if (isSearching) View.VISIBLE else View.GONE
                    binding.emptySearchState.visibility =
                        if (isSearching && model.searchResults!!.isEmpty()) View.VISIBLE else View.GONE

                    if (isSearching) {
                        searchAdapter.submitList(model.searchResults)
                        binding.tvEmptySearchQuery.text =
                            "Nenhum evento encontrado para \"${binding.etSearch.text}\""
                    } else {
                        featuredAdapter.submitList(model.featured)
                        nearbyAdapter.submitList(model.nearby)
                        weekendAdapter.submitList(model.thisWeekend)
                        freeAdapter.submitList(model.free)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

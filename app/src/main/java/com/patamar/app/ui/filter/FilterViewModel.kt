package com.patamar.app.ui.filter

import androidx.lifecycle.ViewModel
import com.patamar.app.data.model.EventCategory
import com.patamar.app.data.model.FilterPreferences
import com.patamar.app.data.repository.FilterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val filterRepository: FilterRepository
) : ViewModel() {

    private val _filters = MutableStateFlow(filterRepository.getFilters())
    val filters: StateFlow<FilterPreferences> = _filters.asStateFlow()

    fun toggleCategory(category: EventCategory) {
        val current = _filters.value.categories
        val updated = if (category in current) current - category else current + category
        _filters.value = _filters.value.copy(categories = updated)
    }

    fun setRadius(radiusMeters: Int) {
        _filters.value = _filters.value.copy(radiusMeters = radiusMeters)
    }

    fun confirm() {
        filterRepository.saveFilters(_filters.value)
    }
}

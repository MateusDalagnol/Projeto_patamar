package com.patamar.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patamar.app.core.security.EncryptedPrefsManager
import com.patamar.app.data.model.Event
import com.patamar.app.data.repository.EventRepository
import com.patamar.app.data.repository.FilterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val filterRepository: FilterRepository,
    private val prefsManager: EncryptedPrefsManager
) : ViewModel() {

    // Filtro rápido (guest / FAB no mapa) — não persistido, só em memória
    private val _quickFilterCategories = MutableStateFlow<Set<com.patamar.app.data.model.EventCategory>>(emptySet())
    val quickFilterCategories: StateFlow<Set<com.patamar.app.data.model.EventCategory>> =
        _quickFilterCategories.asStateFlow()

    private val _visibleEvents = MutableStateFlow<List<Event>>(emptyList())
    val visibleEvents: StateFlow<List<Event>> = _visibleEvents.asStateFlow()

    val userId: String? get() = prefsManager.getSessionUserId()
    val isGuest: Boolean get() = prefsManager.isGuestSession()

    init {
        eventRepository.observeAllEvents()
            .combine(_quickFilterCategories) { events, quickCategories ->
                val savedFilters = filterRepository.getFilters()
                val allCategories = savedFilters.categories + quickCategories
                if (allCategories.isEmpty()) events
                else events.filter { it.category in allCategories }
            }
            .onEach { _visibleEvents.value = it }
            .launchIn(viewModelScope)
    }

    fun applyQuickFilter(categories: Set<com.patamar.app.data.model.EventCategory>) {
        _quickFilterCategories.value = categories
    }
}

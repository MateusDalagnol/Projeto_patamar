package com.patamar.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patamar.app.core.security.EncryptedPrefsManager
import com.patamar.app.core.utils.Constants
import com.patamar.app.data.model.Event
import com.patamar.app.data.model.EventCategory
import com.patamar.app.data.repository.EventRepository
import com.patamar.app.data.repository.FilterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ExploreUiModel(
    val featured: List<Event> = emptyList(),
    val nearby: List<Event> = emptyList(),
    val thisWeekend: List<Event> = emptyList(),
    val free: List<Event> = emptyList(),
    val searchResults: List<Event>? = null // null = não está buscando
)

@OptIn(FlowPreview::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val filterRepository: FilterRepository,
    prefsManager: EncryptedPrefsManager
) : ViewModel() {

    // Visitante não salva eventos (não tem userId real).
    private val userId: String? = prefsManager.getSessionUserId()?.takeIf { !prefsManager.isGuestSession() }
    val canSave: Boolean get() = userId != null

    val savedEventIds: StateFlow<Set<String>> = (
        if (userId == null) flowOf<Set<String>>(emptySet())
        else eventRepository.observeSavedEvents(userId).map { list -> list.map { it.id }.toSet() }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleSaved(event: Event) {
        val id = userId ?: return
        viewModelScope.launch { eventRepository.toggleSaved(id, event.id) }
    }

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<EventCategory?>(null)

    val uiModel: StateFlow<ExploreUiModel> = combine(
        eventRepository.observeAllEvents(),
        _searchQuery.debounce(Constants.SEARCH_DEBOUNCE_MS).distinctUntilChanged(),
        _selectedCategory
    ) { events, query, category ->
        // PATCH v0.1.1: os filtros persistidos em FilterActivity (Filtros do app,
        // os mesmos que já valiam pro Mapa) agora também restringem o Explorar.
        // O chip de categoria da própria tela continua funcionando como um
        // recorte adicional em cima desse filtro salvo.
        val savedCategories = filterRepository.getFilters().categories
        val withinSavedFilter = if (savedCategories.isEmpty()) {
            events
        } else {
            events.filter { it.category in savedCategories }
        }
        val filtered = if (category != null) withinSavedFilter.filter { it.category == category } else withinSavedFilter

        if (query.isNotBlank()) {
            val results = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.category.label.contains(query, ignoreCase = true)
            }
            ExploreUiModel(searchResults = results)
        } else {
            ExploreUiModel(
                featured = filtered.filter { it.isHighlighted },
                nearby = filtered.sortedBy { it.distanceMeters }.take(10),
                thisWeekend = filtered.filter { it.date.dayOfWeek.value >= 5 },
                free = filtered.filter { it.isFree },
                searchResults = null
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExploreUiModel())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: EventCategory?) {
        _selectedCategory.value = category
    }
}

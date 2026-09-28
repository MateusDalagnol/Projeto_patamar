package com.patamar.app.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patamar.app.core.security.EncryptedPrefsManager
import com.patamar.app.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado "salvo" do evento aberto no detalhe. O eventId vem dos argumentos do
// fragment (SavedStateHandle). Visitante não salva.
@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    prefsManager: EncryptedPrefsManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = savedStateHandle[EventDetailContent.ARG_ID] ?: ""
    private val userId: String? = prefsManager.getSessionUserId()?.takeIf { !prefsManager.isGuestSession() }
    val canSave: Boolean get() = userId != null

    val isSaved: StateFlow<Boolean> = (
        if (userId == null) flowOf(false)
        else eventRepository.observeSavedEvents(userId).map { list -> list.any { it.id == eventId } }
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleSaved() {
        val id = userId ?: return
        viewModelScope.launch { eventRepository.toggleSaved(id, eventId) }
    }
}

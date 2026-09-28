package com.patamar.app.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patamar.app.core.security.EncryptedPrefsManager
import com.patamar.app.data.model.Event
import com.patamar.app.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val prefsManager: EncryptedPrefsManager
) : ViewModel() {

    val isGuest: Boolean get() = prefsManager.isGuestSession()
    private val userId: String? get() = prefsManager.getSessionUserId()

    val savedEvents: StateFlow<List<Event>> = (userId?.let { eventRepository.observeSavedEvents(it) }
        ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeEvent(event: Event) {
        val uid = userId ?: return
        viewModelScope.launch { eventRepository.removeSaved(uid, event.id) }
    }
}

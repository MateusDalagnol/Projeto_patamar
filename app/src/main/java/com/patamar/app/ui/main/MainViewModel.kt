package com.patamar.app.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// Compartilhado entre as tabs (escopo de Activity) — carrega o pedido de
// "ver no mapa" do EventDetailBottomSheet (aberto de qualquer tab) até o
// MapFragment, mesmo que ele precise ser criado na troca de tab.
@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _pendingFocusEventId = MutableStateFlow<String?>(null)
    val pendingFocusEventId: StateFlow<String?> = _pendingFocusEventId.asStateFlow()

    fun requestFocusEvent(eventId: String) {
        _pendingFocusEventId.value = eventId
    }

    fun consumeFocusEvent() {
        _pendingFocusEventId.value = null
    }
}

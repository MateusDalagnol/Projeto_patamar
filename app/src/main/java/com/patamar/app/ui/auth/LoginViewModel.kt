package com.patamar.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patamar.app.core.security.EncryptedPrefsManager
import com.patamar.app.core.utils.UiState
import com.patamar.app.data.model.User
import com.patamar.app.data.repository.AuthRepository
import com.patamar.app.data.repository.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginFormState(
    val emailError: String? = null,
    val passwordError: String? = null
)

// Só repassa os campos pra AuthRepository (que valida, autentica e controla o bloqueio
// por tentativas) e traduz o resultado em estado de tela.
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val prefsManager: EncryptedPrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val uiState: StateFlow<UiState<User>> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    fun login(email: String, password: String) {
        if (_uiState.value is UiState.Loading) return
        _formState.value = LoginFormState()
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is LoginResult.Success -> _uiState.value = UiState.Success(result.user)
                is LoginResult.InvalidInput -> {
                    _formState.value = LoginFormState(result.emailError, result.passwordError)
                    _uiState.value = UiState.Idle
                }
                is LoginResult.WrongCredentials -> _uiState.value = UiState.Error(
                    "E-mail ou senha incorretos (${result.attemptsLeft} tentativa(s) restante(s))"
                )
                is LoginResult.Locked -> _uiState.value = UiState.Error(
                    "Muitas tentativas. Tente novamente em ${result.secondsLeft}s."
                )
            }
        }
    }

    fun continueAsGuest() {
        prefsManager.saveSession(userId = "guest_${System.currentTimeMillis()}", isGuest = true)
    }
}

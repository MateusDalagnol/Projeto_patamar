package com.patamar.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patamar.app.core.utils.UiState
import com.patamar.app.data.model.User
import com.patamar.app.data.repository.AuthRepository
import com.patamar.app.data.repository.RegisterRequest
import com.patamar.app.data.repository.RegisterResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterFormState(
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null
)

// Só repassa os campos pra AuthRepository (que valida tudo, checa e-mail duplicado e cria a
// conta) e mostra os erros que ela devolver.
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val uiState: StateFlow<UiState<User>> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

    fun register(name: String, email: String, password: String, confirmPassword: String, termsAccepted: Boolean) {
        if (_uiState.value is UiState.Loading) return
        _formState.value = RegisterFormState()
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            val request = RegisterRequest(name, email, password, confirmPassword, termsAccepted)
            when (val result = authRepository.register(request)) {
                is RegisterResult.Success -> _uiState.value = UiState.Success(result.user)
                is RegisterResult.Invalid -> {
                    val e = result.errors
                    _formState.value = RegisterFormState(e.name, e.email, e.password, e.confirmPassword, e.terms)
                    _uiState.value = UiState.Idle
                }
            }
        }
    }

    fun clearFieldError(field: Field) {
        val current = _formState.value
        _formState.value = when (field) {
            Field.NAME -> current.copy(nameError = null)
            Field.EMAIL -> current.copy(emailError = null)
            Field.PASSWORD -> current.copy(passwordError = null)
            Field.CONFIRM_PASSWORD -> current.copy(confirmPasswordError = null)
            Field.TERMS -> current.copy(termsError = null)
        }
    }

    enum class Field { NAME, EMAIL, PASSWORD, CONFIRM_PASSWORD, TERMS }
}

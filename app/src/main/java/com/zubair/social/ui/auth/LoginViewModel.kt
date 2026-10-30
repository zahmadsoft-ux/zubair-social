package com.zubair.social.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zubair.social.auth.AuthRepository
import com.zubair.social.auth.AuthResult
import com.zubair.social.auth.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Screen ki poori state ek jagah. UI sirf isay dekh kar draw karti hai. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null,   // server/Firebase wali errors
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
)

class LoginViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) =
        _state.update { it.copy(email = value, emailError = null, formError = null) }

    fun onPasswordChange(value: String) =
        _state.update { it.copy(password = value, passwordError = null, formError = null) }

    fun onLoginClick() {
        val current = _state.value
        if (current.isLoading) return

        // Client-side check pehle: khali field ho to network call hi nahi jati.
        val email = current.email.trim()
        val emailError = if (email.isEmpty()) "Enter your email" else null
        val passwordError = if (current.password.isEmpty()) "Enter your password" else null
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _state.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            when (val result = repository.signIn(email, current.password)) {
                AuthResult.Success -> _state.update { it.copy(isLoading = false, isLoggedIn = true) }
                is AuthResult.Failure ->
                    _state.update { it.copy(isLoading = false, formError = result.error.message) }
            }
        }
    }
}

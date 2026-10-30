package com.zubair.social.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zubair.social.auth.AuthRepository
import com.zubair.social.auth.FirebaseAuthRepository
import com.zubair.social.auth.SignUpResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UsernameStatus { Idle, Checking, Available, Taken }

data class SignUpUiState(
    val email: String = "",
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val emailError: String? = null,
    val fullNameError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null,
    val usernameStatus: UsernameStatus = UsernameStatus.Idle,
    val isLoading: Boolean = false,
    val isSignedUp: Boolean = false,
)

class SignUpViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = _state.asStateFlow()

    private var usernameCheckJob: Job? = null

    fun onEmailChange(v: String) = _state.update { it.copy(email = v, emailError = null, formError = null) }
    fun onFullNameChange(v: String) = _state.update { it.copy(fullName = v, fullNameError = null, formError = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, passwordError = null, formError = null) }

    fun onUsernameChange(v: String) {
        _state.update {
            it.copy(username = v, usernameError = null, formError = null, usernameStatus = UsernameStatus.Idle)
        }
        // Step 3: debounced live check. Har keystroke par DB call nahi, 300ms ruk kar.
        usernameCheckJob?.cancel()
        if (usernameFormatError(v) != null) return   // galat format par DB se poochna bekar hai
        usernameCheckJob = viewModelScope.launch {
            delay(300)
            _state.update { it.copy(usernameStatus = UsernameStatus.Checking) }
            val available = repository.isUsernameAvailable(v)
            _state.update {
                it.copy(
                    usernameStatus = when (available) {
                        true -> UsernameStatus.Available
                        false -> UsernameStatus.Taken
                        null -> UsernameStatus.Idle
                    }
                )
            }
        }
    }

    fun onSignUpClick() {
        val s = _state.value
        if (s.isLoading) return

        // Step 5: client-side validation, network se pehle.
        val email = s.email.trim()
        val fullName = s.fullName.trim()
        val username = s.username.trim()
        val emailError = emailError(email)
        val fullNameError = if (fullName.isEmpty()) "Enter your full name" else null
        val usernameError = usernameFormatError(username)
        val passwordError = when {
            s.password.isEmpty() -> "Enter a password"
            s.password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        if (listOf(emailError, fullNameError, usernameError, passwordError).any { it != null }) {
            _state.update {
                it.copy(
                    emailError = emailError, fullNameError = fullNameError,
                    usernameError = usernameError, passwordError = passwordError,
                )
            }
            return
        }

        _state.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            // Steps 6-10 repository ke andar hote hain.
            when (val result = repository.signUp(email, fullName, username, s.password)) {
                SignUpResult.Success -> _state.update { it.copy(isLoading = false, isSignedUp = true) }
                is SignUpResult.Failure -> _state.update {
                    it.copy(
                        isLoading = false,
                        formError = result.error.message,
                        usernameStatus = if (result.error == com.zubair.social.auth.SignUpError.UsernameTaken)
                            UsernameStatus.Taken else it.usernameStatus,
                    )
                }
            }
        }
    }

    private fun emailError(email: String): String? = when {
        email.isEmpty() -> "Enter your email"
        !EMAIL_REGEX.matches(email) -> "Enter a valid email address"
        else -> null
    }

    private fun usernameFormatError(username: String): String? = when {
        username.isEmpty() -> "Choose a username"
        !USERNAME_CHARS.matches(username) -> "Only letters, numbers, and underscores are allowed"
        username.length < 3 || username.length > 20 -> "Username must be 3-20 characters"
        else -> null
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        val USERNAME_CHARS = Regex("^[A-Za-z0-9_]+$")
    }
}

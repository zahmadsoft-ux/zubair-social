package com.zubair.social.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.zubair.social.profile.FirebaseProfileRepository
import com.zubair.social.profile.ProfileRepository
import com.zubair.social.profile.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val error: String? = null,
    // Posts/followers/following ka data abhi nahi hai (Create Post / Follow features baad mein), isliye 0.
    val posts: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
)

class ProfileViewModel(
    private val repository: ProfileRepository = FirebaseProfileRepository(),
    private val uid: String? = FirebaseAuth.getInstance().currentUser?.uid,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        if (uid == null) {
            _state.update { it.copy(isLoading = false, error = "You are not signed in") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val profile = repository.getProfile(uid)
            _state.update {
                if (profile == null) it.copy(isLoading = false, error = "Couldn't load your profile. Check your connection and try again.")
                else it.copy(isLoading = false, profile = profile)
            }
        }
    }
}

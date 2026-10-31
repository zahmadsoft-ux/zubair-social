package com.zubair.social.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.zubair.social.profile.FirebaseProfileRepository
import com.zubair.social.profile.ProfileRepository
import com.zubair.social.profile.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val BIO_MAX_LENGTH = 150
const val FULL_NAME_MAX_LENGTH = 50

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val original: UserProfile? = null,
    val fullName: String = "",
    val bio: String = "",
    /** Photo Picker se chuni hui image (preview only; upload Create Post ke baad). */
    val pickedAvatarUri: String? = null,
    val fullNameError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saved: Boolean = false,
) {
    /** Sirf name/bio count hote hain: avatar abhi save nahi hota. */
    val hasChanges: Boolean
        get() = original != null &&
            (fullName.trim() != original.fullName || bio.trim() != original.bio)
}

class EditProfileViewModel(
    private val repository: ProfileRepository = FirebaseProfileRepository(),
    private val uid: String? = FirebaseAuth.getInstance().currentUser?.uid,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        if (uid == null) {
            _state.update { it.copy(isLoading = false, loadError = "You are not signed in") }
            return
        }
        _state.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            try {
                val user = repository.getUser(uid)
                _state.update {
                    it.copy(isLoading = false, original = user, fullName = user.fullName, bio = user.bio)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, loadError = "Couldn't load your profile. Check your connection and try again.") }
            }
        }
    }

    fun onFullNameChange(value: String) =
        _state.update { it.copy(fullName = value.take(FULL_NAME_MAX_LENGTH), fullNameError = null, saveError = null) }

    // 150 se zyada type hi nahi hone dete (doc: client-side hi rok do).
    fun onBioChange(value: String) =
        _state.update { it.copy(bio = value.take(BIO_MAX_LENGTH), saveError = null) }

    fun onAvatarPicked(uri: String?) {
        if (uri != null) _state.update { it.copy(pickedAvatarUri = uri) }
    }

    fun onSaveClick() {
        val current = _state.value
        val original = current.original ?: return
        if (current.isSaving || uid == null) return

        val fullName = current.fullName.trim()
        val bio = current.bio.trim()
        if (fullName.isEmpty()) {
            _state.update { it.copy(fullNameError = "Enter your full name") }
            return
        }

        // Sirf badli hui fields: poora object overwrite nahi (username/createdAt safe rehte hain).
        val changes = buildMap<String, Any> {
            if (fullName != original.fullName) put("fullName", fullName)
            if (bio != original.bio) put("bio", bio)
        }
        if (changes.isEmpty()) {
            _state.update { it.copy(saved = true) }
            return
        }

        _state.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            try {
                repository.updateProfile(uid, changes)
                _state.update { it.copy(isSaving = false, saved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Form band nahi hota: user ka likha hua state mein rehta hai, dobara try kar sakta hai.
                _state.update { it.copy(isSaving = false, saveError = "Couldn't save your changes. Check your connection and try again.") }
            }
        }
    }
}

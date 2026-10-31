package com.zubair.social.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.zubair.social.profile.FirebaseProfileRepository
import com.zubair.social.profile.PostThumb
import com.zubair.social.profile.ProfileNotFoundException
import com.zubair.social.profile.ProfileRepository
import com.zubair.social.profile.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Screen ki teen halatein: load ho raha hai, data aa gaya, ya fail. */
sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Success(
        val profile: UserProfile,
        val posts: List<PostThumb>,
        val followers: Int,
        val following: Int,
        val isOwnProfile: Boolean,
        val isFollowing: Boolean,
    ) : ProfileUiState

    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val uid: String,
    private val repository: ProfileRepository = FirebaseProfileRepository(),
    private val currentUid: String? = FirebaseAuth.getInstance().currentUser?.uid,
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init { load() }

    /** Edit se wapas aane par: purana data dikhta rahe (skeleton flash nahi), naya aate hi badal jaye. */
    fun refresh() = load(showLoading = false)

    fun load(showLoading: Boolean = true) {
        if (showLoading) _state.value = ProfileUiState.Loading
        viewModelScope.launch {
            val result = try {
                val data = repository.load(uid, currentUid)
                ProfileUiState.Success(
                    profile = data.profile,
                    posts = data.posts,
                    followers = data.followers,
                    following = data.following,
                    isOwnProfile = uid == currentUid,
                    isFollowing = data.isFollowing,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: ProfileNotFoundException) {
                ProfileUiState.Error("This profile doesn't exist")
            } catch (e: Exception) {
                ProfileUiState.Error("Couldn't load the profile. Check your connection and try again.")
            }
            // Silent refresh fail ho to pehle wala data hi rehne do.
            if (showLoading || result is ProfileUiState.Success) _state.value = result
        }
    }
}

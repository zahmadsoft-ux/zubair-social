package com.zubair.social.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zubair.social.feed.FeedAssembler
import com.zubair.social.feed.FeedCursor
import com.zubair.social.feed.FeedPage
import com.zubair.social.feed.FeedPost
import com.zubair.social.feed.FirebaseLikeRepository
import com.zubair.social.feed.LikeRepository
import com.zubair.social.feed.withLiked
import com.zubair.social.feed.FeedRepository
import com.zubair.social.feed.FirebaseFeedRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data object Empty : FeedUiState
    data class Success(
        val posts: List<FeedPost>,
        val isLoadingMore: Boolean = false,   // neeche footer spinner
        val endReached: Boolean = false,      // true: aur purane posts nahi, loading band
        val loadMoreFailed: Boolean = false,  // footer par "Try again"
    ) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

class FeedViewModel(
    private val repository: FeedRepository = FirebaseFeedRepository(),
    private val likes: LikeRepository = FirebaseLikeRepository(),
) : ViewModel() {

    // Ek-baar ke error messages (Toast) — state mein nahi, warna rotation par dobara dikhte.
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    // Jin posts ka like request abhi server par chal raha hai (double-tap guard + refresh se bachav).
    private val pendingLikes = mutableSetOf<String>()

    private val _state = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    // Agla page isi se shuru hoga (ab tak ki sabse purani feed entry).
    private var cursor: FeedCursor? = null
    // Main thread par hi badalta hai: refresh/loadMore ek waqt mein ek hi chale.
    private var busy = false

    init { load() }

    /** Tab par wapas aane par: purani feed dikhti rahe, naye posts upar judte jayen. */
    fun refresh() {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try {
                val page = repository.loadPage()
                val old = _state.value
                if (old is FeedUiState.Success) {
                    // Pehle se loaded purane pages mat phenko: naye upar merge, cursor/endReached purane hi rahen.
                    // Jis post ka like abhi in-flight hai uska optimistic roop rakho, server ka purana nahi.
                    val fresh = page.posts.map { p -> old.posts.firstOrNull { it.postId == p.postId && it.postId in pendingLikes } ?: p }
                    _state.value = old.copy(posts = FeedAssembler.merge(old.posts, fresh))
                } else {
                    showFirstPage(page)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Silent refresh fail ho to pehle wali feed hi rehne do.
                if (_state.value is FeedUiState.Loading) _state.value = FeedUiState.Error(LOAD_ERROR)
            } finally {
                busy = false
            }
        }
    }

    /** Pehli/explicit load (retry): sab reset. */
    fun load() {
        _state.value = FeedUiState.Loading
        cursor = null
        busy = true
        viewModelScope.launch {
            try {
                showFirstPage(repository.loadPage())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = FeedUiState.Error(LOAD_ERROR)
            } finally {
                busy = false
            }
        }
    }

    /** Neeche scroll par: cursor se pehle ke agle 20 posts. End par ya busy ho to kuch nahi. */
    fun loadMore() {
        val current = _state.value as? FeedUiState.Success ?: return
        if (busy || current.endReached) return
        busy = true
        _state.value = current.copy(isLoadingMore = true, loadMoreFailed = false)
        viewModelScope.launch {
            try {
                val page = repository.loadPage(before = cursor)
                cursor = page.nextCursor ?: cursor
                val latest = _state.value as? FeedUiState.Success ?: return@launch
                _state.value = latest.copy(
                    posts = FeedAssembler.merge(latest.posts, page.posts),   // duplicate postId nahi
                    isLoadingMore = false,
                    endReached = page.endReached,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                (_state.value as? FeedUiState.Success)?.let {
                    _state.value = it.copy(isLoadingMore = false, loadMoreFailed = true)
                }
            } finally {
                busy = false
            }
        }
    }

    /** Heart tap: UI turant badlo, phir server; fail par wapas. Same post par request chal rahi ho to tap ignore. */
    fun onLikeClick(postId: String) {
        val post = (_state.value as? FeedUiState.Success)?.posts?.firstOrNull { it.postId == postId } ?: return
        if (!pendingLikes.add(postId)) return
        val wantLiked = !post.isLiked
        updatePost(postId) { it.withLiked(wantLiked) }   // optimistic: icon filled + count +1
        viewModelScope.launch {
            try {
                likes.setLiked(postId, post.uid, wantLiked)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updatePost(postId) { it.withLiked(!wantLiked) }   // revert
                _events.tryEmit("Couldn't update your like. Try again.")
            } finally {
                pendingLikes.remove(postId)
            }
        }
    }

    private fun updatePost(postId: String, transform: (FeedPost) -> FeedPost) {
        _state.update { s ->
            if (s is FeedUiState.Success) s.copy(posts = s.posts.map { if (it.postId == postId) transform(it) else it }) else s
        }
    }

    private fun showFirstPage(page: FeedPage) {
        cursor = page.nextCursor
        _state.value = if (page.posts.isEmpty()) FeedUiState.Empty
        else FeedUiState.Success(page.posts, endReached = page.endReached)
    }

    private companion object {
        const val LOAD_ERROR = "Couldn't load your feed. Check your connection and try again."
    }
}

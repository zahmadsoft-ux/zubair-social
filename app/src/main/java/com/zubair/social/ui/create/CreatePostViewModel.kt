package com.zubair.social.ui.create

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.zubair.social.media.ImageCompressor
import com.zubair.social.net.isOnline
import com.zubair.social.post.FirebasePostRepository
import com.zubair.social.post.PostRepository
import com.zubair.social.upload.UnauthorizedException
import com.zubair.social.upload.UploadService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

const val CAPTION_MAX_LENGTH = 2200

/** Share dabane ke baad ka safar: compress -> Worker se URL -> R2 par PUT -> RTDB write -> Done. */
sealed interface UploadState {
    data object Idle : UploadState
    data object Preparing : UploadState
    data class Uploading(val percent: Int) : UploadState
    /** Image R2 par pahunch chuki, ab teeno RTDB paths likhe ja rahe hain. */
    data object Saving : UploadState
    data class Done(val postId: String) : UploadState
    /** Retry dabane par kya dobara hoga, ye ViewModel ke pendingImageUrl se tay hota hai (upload ya sirf RTDB write). */
    data class Failed(val message: String) : UploadState
}

private class NoInternetException : IOException("No internet")

data class CreatePostUiState(
    /** Photo Picker se mili content:// Uri (string). null = abhi koi image nahi chuni. */
    val imageUri: String? = null,
    val caption: String = "",
    val upload: UploadState = UploadState.Idle,
) {
    /** Image ho aur koi upload chal nahi raha. */
    val canShare: Boolean
        get() = imageUri != null && upload is UploadState.Idle

    /** "Uploading" screen dikhani hai? */
    val isUploadScreen: Boolean
        get() = upload !is UploadState.Idle
}

// @JvmOverloads: Android ka default ViewModel factory sirf (Application) constructor dhoondta hai;
// iske bina defaults wale extra parameters ke saath "Cannot create an instance" crash hota hai.
class CreatePostViewModel @JvmOverloads constructor(
    application: Application,
    private val uploadService: UploadService = UploadService(),
    private val postRepository: PostRepository = FirebasePostRepository(),
    private val currentUid: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    /** forceRefresh = true ho to Firebase naya token mangta hai (purana expired ho to). */
    private val idToken: suspend (forceRefresh: Boolean) -> String? = { force ->
        FirebaseAuth.getInstance().currentUser?.getIdToken(force)?.await()?.token
    },
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(CreatePostUiState())
    val state: StateFlow<CreatePostUiState> = _state.asStateFlow()

    // Double-tap guard: UI ka button disabled hone se pehle do tap aa sakte hain, isliye
    // yahan atomic flag hai. compareAndSet sirf pehle tap ko true deta hai.
    // Double-tap guard: UI ka button disabled hone se pehle do tap aa sakte hain, isliye
    // yahan atomic flag hai. compareAndSet sirf pehle tap ko true deta hai.
    private val inFlight = AtomicBoolean(false)
    private var uploadJob: Job? = null

    // Upload ho chuka to yahan yaad rehta hai: RTDB write fail ho to Retry sirf write dobara karta hai,
    // image dobara upload nahi hoti. postId bhi wahi rehta hai taake duplicate post na bane.
    private var pendingImageUrl: String? = null
    private var pendingPostId: String? = null

    /** Picker cancel ho to uri null aata hai: pehle wali image rehne do. */
    fun onImagePicked(uri: String?) {
        if (uri == null) return
        clearPending()   // nayi image = naya post
        _state.update { it.copy(imageUri = uri, upload = UploadState.Idle) }
    }

    // 2200 se zyada type hi nahi hone dete.
    fun onCaptionChange(value: String) =
        _state.update { it.copy(caption = value.take(CAPTION_MAX_LENGTH)) }

    fun onShareClick() {
        if (!_state.value.canShare) return
        start()
    }

    /** Failed screen ka Retry: pendingImageUrl ho to sirf RTDB write, warna poora upload dobara. */
    fun onRetryClick() {
        if (_state.value.upload !is UploadState.Failed) return
        start()
    }

    private fun start() {
        val uri = _state.value.imageUri ?: return
        if (!inFlight.compareAndSet(false, true)) return   // doosra tap yahin ruk jata hai

        uploadJob = viewModelScope.launch {
            var uploadDone = pendingImageUrl != null
            try {
                val imageUrl = pendingImageUrl ?: uploadImage(uri).also {
                    pendingImageUrl = it
                    uploadDone = true
                }

                _state.update { it.copy(upload = UploadState.Saving) }
                val uid = currentUid() ?: throw IOException("Not signed in")
                val postId = pendingPostId ?: postRepository.newPostId().also { pendingPostId = it }
                postRepository.publish(postId, uid, imageUrl, _state.value.caption.trim())

                clearPending()
                _state.update { it.copy(upload = UploadState.Done(postId)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("CreatePost", "Share failed (uploadDone=$uploadDone)", e)
                _state.update { it.copy(upload = UploadState.Failed(e.toUserMessage(uploadDone))) }
            } finally {
                inFlight.set(false)
            }
        }
    }

    /** Network check -> compress -> Worker se URL -> R2 PUT. Final public imageUrl lautata hai. */
    private suspend fun uploadImage(uri: String): String {
        _state.update { it.copy(upload = UploadState.Preparing) }
        if (!getApplication<Application>().isOnline()) throw NoInternetException()

        val image = ImageCompressor.compress(getApplication(), Uri.parse(uri))

        val target = try {
            uploadService.getUploadTarget(token(force = false))
        } catch (_: UnauthorizedException) {
            uploadService.getUploadTarget(token(force = true))   // token expire tha: ek baar refresh karke retry
        }

        _state.update { it.copy(upload = UploadState.Uploading(0)) }
        uploadService.upload(target, image.bytes) { percent ->
            _state.update { it.copy(upload = UploadState.Uploading(percent)) }
        }
        return target.imageUrl
    }

    /** Cancel (Uploading ya Failed screen se): form wapas, image/caption bachi rehti hain. */
    fun onCancelUpload() {
        uploadJob?.cancel()
        inFlight.set(false)
        // Agar image R2 par pahunch chuki thi to wo wahan orphan reh jati hai (abhi cleanup nahi hai).
        clearPending()
        _state.update { it.copy(upload = UploadState.Idle) }
    }

    /** Success ke baad (navigate karne par): naya khali form. */
    fun onUploadFinished() {
        clearPending()
        _state.value = CreatePostUiState()
    }

    private fun clearPending() {
        pendingImageUrl = null
        pendingPostId = null
    }

    private suspend fun token(force: Boolean): String =
        idToken(force) ?: throw IOException("Not signed in")

    private fun Exception.toUserMessage(uploadDone: Boolean): String = when {
        this is NoInternetException -> "No internet connection. Check it and try again."
        // Image R2 par hai, sirf post record nahi bana: Retry sirf RTDB write karega.
        uploadDone -> "Your photo was uploaded, but the post couldn't be saved. Try again."
        this is UnauthorizedException -> "Your session has expired. Please log in again."
        this is IOException -> "Upload failed. Check your connection and try again."
        else -> "Couldn't prepare your photo. Try a different one."
    }
}

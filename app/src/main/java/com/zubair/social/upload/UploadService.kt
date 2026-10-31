package com.zubair.social.upload

import com.zubair.social.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSink
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Worker ka jawab: PUT yahan karo, aur upload ke baad image is public URL par milegi. */
data class UploadTarget(val uploadUrl: String, val imageUrl: String, val contentType: String)

/** Worker ne Firebase token reject kiya (expired / galat). Caller token refresh karke ek baar dobara try kar sakta hai. */
class UnauthorizedException : IOException("Worker rejected the token")

/**
 * RequestBody jo bytes chunk-chunk likhta hai aur har chunk ke baad percent (0-100) batata hai.
 * Percent sirf tab callback hota hai jab value badle, taake UI ko 8KB ke har chunk par recompose na karna pare.
 * Note: "written" wo bytes hain jo socket ko diye gaye; server tak pahunchne se thoda aage ho sakte hain.
 */
class ProgressRequestBody(
    private val bytes: ByteArray,
    private val contentType: MediaType?,
    private val onProgress: (Int) -> Unit,
) : RequestBody() {
    override fun contentType() = contentType
    override fun contentLength() = bytes.size.toLong()

    override fun writeTo(sink: BufferedSink) {
        var written = 0
        var lastPercent = -1
        onProgress(0)
        while (written < bytes.size) {
            val n = minOf(CHUNK_SIZE, bytes.size - written)
            sink.write(bytes, written, n)
            sink.flush()
            written += n
            val percent = (written * 100L / bytes.size).toInt()
            if (percent != lastPercent) {
                lastPercent = percent
                onProgress(percent)
            }
        }
    }

    private companion object { const val CHUNK_SIZE = 8 * 1024 }
}

class UploadService(
    private val client: OkHttpClient = OkHttpClient(),
    private val workerUrl: String = BuildConfig.UPLOAD_WORKER_URL,
) {
    /** Step 1: Firebase ID token ke saath Worker se presigned URL maango. */
    suspend fun getUploadTarget(idToken: String): UploadTarget {
        val request = Request.Builder()
            .url("${workerUrl.trimEnd('/')}/get-upload-url")
            .header("Authorization", "Bearer $idToken")
            .post(ByteArray(0).toRequestBody())
            .build()
        client.newCall(request).await().use { response ->
            if (response.code == 401) throw UnauthorizedException()
            if (!response.isSuccessful) throw IOException("Worker error ${response.code}")
            val json = JSONObject(response.body?.string() ?: throw IOException("Empty Worker response"))
            return UploadTarget(
                uploadUrl = json.getString("uploadUrl"),
                imageUrl = json.getString("imageUrl"),
                contentType = json.optString("contentType", "image/jpeg"),
            )
        }
    }

    /** Step 2: bytes seedha R2 ko PUT karo (Worker se nahi guzarte). Content-Type wahi jo URL sign hote waqt tha. */
    suspend fun upload(target: UploadTarget, bytes: ByteArray, onProgress: (Int) -> Unit) {
        val body = ProgressRequestBody(bytes, target.contentType.toMediaType(), onProgress)
        val request = Request.Builder().url(target.uploadUrl).put(body).build()
        client.newCall(request).await().use { response ->
            if (!response.isSuccessful) throw IOException("Upload failed: ${response.code}")
        }
    }
}

/** OkHttp ki enqueue ko coroutine banata hai; coroutine cancel ho to network call bhi cancel. */
private suspend fun Call.await(): Response = suspendCancellableCoroutine { cont ->
    cont.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (cont.isActive) cont.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            cont.resume(response) { response.close() }
        }
    })
}

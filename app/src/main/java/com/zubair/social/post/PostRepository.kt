package com.zubair.social.post

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.zubair.social.auth.FirebaseAuthRepository
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

/** Teeno RTDB paths ka data ek jagah. Pure function: unit test ho sakta hai (timestamp bahar se aata hai). */
object PostPaths {
    fun updates(postId: String, uid: String, imageUrl: String, caption: String, timestamp: Any): Map<String, Any> = mapOf(
        "posts/$postId" to mapOf(
            "uid" to uid,
            "imageUrl" to imageUrl,
            "caption" to caption,
            "createdAt" to timestamp,
            "likeCount" to 0,
        ),
        "userPosts/$uid/$postId" to true,
        "feedPosts/$postId" to mapOf("uid" to uid, "createdAt" to timestamp),
    )
}

interface PostRepository {
    /** Pehle se ID banao taake retry par wahi post dobara likha jaye (duplicate post nahi). */
    fun newPostId(): String

    /** Teeno paths ek hi multi-path updateChildren se: ya sab likhe jate hain, ya kuch bhi nahi. Fail par exception. */
    suspend fun publish(postId: String, uid: String, imageUrl: String, caption: String)
}

class FirebasePostRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(FirebaseAuthRepository.DATABASE_URL),
) : PostRepository {

    override fun newPostId(): String = db.getReference("posts").push().key
        ?: throw IOException("Couldn't create a post id")

    override suspend fun publish(postId: String, uid: String, imageUrl: String, caption: String) {
        val updates = PostPaths.updates(postId, uid, imageUrl, caption, ServerValue.TIMESTAMP)
        // Offline write RTDB queue kar leta hai aur await() ruka rehta hai: timeout par fail dikhao.
        withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            db.reference.updateChildren(updates).await()
        } ?: throw IOException("Timed out saving the post")
    }

    private companion object { const val WRITE_TIMEOUT_MS = 15_000L }
}

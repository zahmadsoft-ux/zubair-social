package com.zubair.social.feed

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.DataSnapshot
import com.zubair.social.auth.FirebaseAuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface LikeRepository {
    /** Fail par exception throw karta hai (tab server par koi like change nahi rehta). */
    suspend fun setLiked(postId: String, ownerUid: String, liked: Boolean)
}

/**
 * Teen jagah likhta hai:
 *  1. /postLikes/{postId}/{uid}  — transaction: ek user ka ek hi like (dobara like = no-op)
 *  2. /posts/{postId}/likeCount  — transaction: +1 / -1 (kabhi 0 se neeche nahi)
 *  3. /notifications/{ownerUid}/like_{postId}_{uid} — sirf dusre ke post par
 * Step 2 fail ho to step 1 wapas palat dete hain, taake like aur count alag na ho jayen.
 */
class FirebaseLikeRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(FirebaseAuthRepository.DATABASE_URL),
    private val currentUid: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
) : LikeRepository {

    override suspend fun setLiked(postId: String, ownerUid: String, liked: Boolean) {
        val uid = currentUid() ?: throw IllegalStateException("Not signed in")
        val likeRef = db.getReference("postLikes/$postId/$uid")

        // false = already usi halat mein (jaise doosre device se like ho chuka): count dobara mat badlo.
        if (!likeRef.transact { data -> toggleLike(data, liked) }) return

        try {
            db.getReference("posts/$postId/likeCount").transact { data ->
                data.value = ((data.getValue(Long::class.java) ?: 0L) + if (liked) 1 else -1).coerceAtLeast(0L)
                Transaction.success(data)
            }
        } catch (e: Exception) {
            // Count nahi badla: like node palat do (best effort) aur UI ko error batao.
            try { likeRef.transact { data -> toggleLike(data, !liked) } } catch (_: Exception) { }
            throw e
        }

        // Notification best effort: iska fail hona like ko fail nahi karta.
        if (ownerUid != uid) {
            try { writeNotification(ownerUid, postId, uid, liked) } catch (e: CancellationException) { throw e } catch (_: Exception) { }
        }
    }

    private fun toggleLike(data: MutableData, liked: Boolean): Transaction.Result {
        val exists = data.value != null
        if (exists == liked) return Transaction.abort()   // pehle se wahi halat
        data.value = if (liked) true else null
        return Transaction.success(data)
    }

    private suspend fun writeNotification(ownerUid: String, postId: String, fromUid: String, liked: Boolean) {
        // Fixed key: baar baar like/unlike par notifications ka dher nahi lagta; unlike par hata dete hain.
        val ref = db.getReference("notifications/$ownerUid/like_${postId}_$fromUid")
        val done = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            if (liked) {
                ref.setValue(mapOf(
                    "type" to "like", "fromUid" to fromUid, "postId" to postId,
                    "createdAt" to ServerValue.TIMESTAMP, "read" to false,
                )).await()
            } else {
                ref.removeValue().await()
            }
            true   // Task<Void>.await() null deta hai, isliye explicit true
        }
        if (done == null) throw IOException("Notification write timed out")
    }

    /** runTransaction ka suspend wrapper. Offline par transaction server ke bina complete nahi hota, isliye timeout. */
    private suspend fun DatabaseReference.transact(handler: (MutableData) -> Transaction.Result): Boolean {
        val committed = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData) = handler(currentData)
                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                        if (!cont.isActive) return
                        if (error != null) cont.resumeWithException(error.toException()) else cont.resume(committed)
                    }
                })
            }
        }
        return committed ?: throw IOException("Transaction timed out")
    }

    private companion object { const val WRITE_TIMEOUT_MS = 10_000L }
}

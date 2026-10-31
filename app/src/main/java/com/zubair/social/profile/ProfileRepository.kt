package com.zubair.social.profile

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.zubair.social.auth.FirebaseAuthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

data class UserProfile(
    val username: String,
    val fullName: String,
    val bio: String,
    val avatarUrl: String?,
)

/** Grid ka ek thumbnail. */
data class PostThumb(val id: String, val imageUrl: String, val createdAt: Long)

data class ProfileData(
    val profile: UserProfile,
    val posts: List<PostThumb>,
    val followers: Int,
    val following: Int,
    /** Viewer ne is user ko follow kiya hua hai? Apne profile par hamesha false. */
    val isFollowing: Boolean,
)

class ProfileNotFoundException : Exception("Profile not found")

interface ProfileRepository {
    /** Fail par exception throw karta hai (network/rules = generic, user node na ho = ProfileNotFoundException). */
    suspend fun load(uid: String, viewerUid: String?): ProfileData

    /** Edit form ko prefill karne ke liye sirf /users/{uid}. */
    suspend fun getUser(uid: String): UserProfile

    /** Sirf di gayi keys update hoti hain (updateChildren), baaki fields (username, createdAt...) untouched. */
    suspend fun updateProfile(uid: String, changes: Map<String, Any>)
}

/**
 * Doc ke "Profile Load" steps: /users, /userPosts aur /follows ek saath (parallel),
 * phir har postId ke liye /posts/{postId} (N+1 reads, MVP ke liye theek hai).
 */
class FirebaseProfileRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(FirebaseAuthRepository.DATABASE_URL),
) : ProfileRepository {

    override suspend fun load(uid: String, viewerUid: String?): ProfileData = coroutineScope {
        val userJob = async { db.getReference("users/$uid").get().await() }
        val postIdsJob = async { db.getReference("userPosts/$uid").get().await() }
        val followersJob = async { db.getReference("follows/$uid/followers").get().await() }
        val followingJob = async { db.getReference("follows/$uid/following").get().await() }
        // /follows/{me}/following/{other}: apne profile par check ki zaroorat hi nahi.
        val isFollowingJob = if (viewerUid != null && viewerUid != uid) {
            async { db.getReference("follows/$viewerUid/following/$uid").get().await().exists() }
        } else null

        val user = userJob.await()
        if (!user.exists()) throw ProfileNotFoundException()
        val profile = user.toUserProfile()

        val postIds = postIdsJob.await().children.mapNotNull { it.key }
        val posts = postIds
            .map { id -> async { db.getReference("posts/$id").get().await() } }
            .awaitAll()
            .mapNotNull { snap ->
                // Post delete ho chuka ho ya image na ho to grid se skip.
                val url = snap.child("imageUrl").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                val id = snap.key
                if (!snap.exists() || url == null || id == null) null
                else PostThumb(id, url, snap.child("createdAt").getValue(Long::class.java) ?: 0L)
            }
            .sortedByDescending { it.createdAt }   // newest pehle

        ProfileData(
            profile = profile,
            posts = posts,
            followers = followersJob.await().childrenCount.toInt(),
            following = followingJob.await().childrenCount.toInt(),
            isFollowing = isFollowingJob?.await() ?: false,
        )
    }

    override suspend fun getUser(uid: String): UserProfile {
        val snap = db.getReference("users/$uid").get().await()
        if (!snap.exists()) throw ProfileNotFoundException()
        return snap.toUserProfile()
    }

    override suspend fun updateProfile(uid: String, changes: Map<String, Any>) {
        // RTDB offline hone par write queue mein chala jata hai aur await() kabhi khatam nahi hota,
        // isliye timeout lagaya: form ko "fail" dikha sakein.
        // withTimeout ki timeout CancellationException hoti hai (callers usay rethrow kar dete hain),
        // isliye withTimeoutOrNull + IOException: caller ko asli "fail" mile.
        withTimeoutOrNull(WRITE_TIMEOUT_MS) {
            db.getReference("users/$uid").updateChildren(changes).await()
            true   // await() ka result null hota hai; true na ho to success bhi timeout lagta hai
        } ?: throw IOException("Timed out saving profile")
    }

    private fun DataSnapshot.toUserProfile() = UserProfile(
        username = child("username").getValue(String::class.java).orEmpty(),
        fullName = child("fullName").getValue(String::class.java).orEmpty(),
        bio = child("bio").getValue(String::class.java).orEmpty(),
        avatarUrl = child("avatarUrl").getValue(String::class.java)?.takeIf { it.isNotBlank() },
    )

    private companion object {
        const val WRITE_TIMEOUT_MS = 10_000L
    }
}

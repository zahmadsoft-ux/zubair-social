package com.zubair.social.profile

import com.google.firebase.database.FirebaseDatabase
import com.zubair.social.auth.FirebaseAuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val username: String,
    val fullName: String,
    val bio: String,
    val avatarUrl: String?,
)

interface ProfileRepository {
    /** null = load fail (network/rules) ya user node maujood nahi. */
    suspend fun getProfile(uid: String): UserProfile?
}

/** /users/{uid} ek baar read karta hai (docs: MVP mein real-time listener zaroori nahi). */
class FirebaseProfileRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(FirebaseAuthRepository.DATABASE_URL),
) : ProfileRepository {
    override suspend fun getProfile(uid: String): UserProfile? = try {
        val snap = db.getReference("users/$uid").get().await()
        if (!snap.exists()) null else UserProfile(
            username = snap.child("username").getValue(String::class.java).orEmpty(),
            fullName = snap.child("fullName").getValue(String::class.java).orEmpty(),
            bio = snap.child("bio").getValue(String::class.java).orEmpty(),
            avatarUrl = snap.child("avatarUrl").getValue(String::class.java)?.takeIf { it.isNotBlank() },
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}

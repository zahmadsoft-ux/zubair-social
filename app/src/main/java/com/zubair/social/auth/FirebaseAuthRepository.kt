package com.zubair.social.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Asli Firebase implementation. ViewModels sirf AuthRepository interface jaante hain,
 * isliye Fake se is par switch karne ke liye screens badalni nahi padin.
 *
 * Database URL explicit diya hai: google-services.json us waqt download hui thi jab RTDB bani nahi thi,
 * isliye us mein firebase_url nahi hai.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL),
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): AuthResult = try {
        auth.signInWithEmailAndPassword(email, password).await()
        AuthResult.Success
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AuthResult.Failure(e.toSignInError())
    }

    override suspend fun isUsernameAvailable(username: String): Boolean? = try {
        !usernameExists(username)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    override suspend fun signUp(
        email: String, fullName: String, username: String, password: String,
    ): SignUpResult {
        val handle = username.lowercase()   // lowercase store: baad mein search case-insensitive lage

        // Step 6: Auth account banane se pehle username check.
        try {
            if (usernameExists(handle)) return SignUpResult.Failure(SignUpError.UsernameTaken)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return SignUpResult.Failure(e.toSignUpError())
        }

        // Steps 7-9: Auth account.
        val user = try {
            auth.createUserWithEmailAndPassword(email, password).await().user
                ?: return SignUpResult.Failure(SignUpError.Unknown)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return SignUpResult.Failure(e.toSignUpError())
        }

        // Step 10: dono writes ek hi multi-path update mein: ya dono hon ya koi nahi.
        return try {
            db.reference.updateChildren(
                mapOf(
                    "users/${user.uid}" to mapOf(
                        "username" to handle,
                        "fullName" to fullName,
                        "bio" to "",
                        "avatarUrl" to "",
                        "createdAt" to System.currentTimeMillis(),
                    ),
                    "usernames/$handle" to user.uid,
                )
            ).await()
            SignUpResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Auth user ban chuka hai lekin profile nahi: cleanup, warna user adha-adhura account ke saath atak jata hai.
            runCatching { user.delete().await() }
            val raced = runCatching { usernameExists(handle) }.getOrDefault(false)
            SignUpResult.Failure(if (raced) SignUpError.UsernameTaken else e.toSignUpError())
        }
    }

    private suspend fun usernameExists(handle: String): Boolean =
        db.getReference("usernames/${handle.lowercase()}").get().await().exists()

    private fun Exception.toSignInError(): AuthError = when (this) {
        is FirebaseAuthInvalidUserException ->
            if (errorCode == "ERROR_USER_DISABLED") AuthError.UserDisabled else AuthError.InvalidCredentials
        is FirebaseAuthInvalidCredentialsException ->
            if (errorCode == "ERROR_INVALID_EMAIL") AuthError.InvalidEmail else AuthError.InvalidCredentials
        is FirebaseTooManyRequestsException -> AuthError.TooManyRequests
        is FirebaseNetworkException -> AuthError.Network
        else -> AuthError.Unknown
    }

    private fun Exception.toSignUpError(): SignUpError = when (this) {
        is FirebaseAuthUserCollisionException -> SignUpError.EmailInUse
        is FirebaseAuthWeakPasswordException -> SignUpError.WeakPassword
        is FirebaseAuthInvalidCredentialsException -> SignUpError.InvalidEmail
        is FirebaseNetworkException -> SignUpError.Network
        else -> SignUpError.Unknown
    }

    private companion object {
        const val DATABASE_URL = "https://zubair-social-default-rtdb.firebaseio.com"
    }
}

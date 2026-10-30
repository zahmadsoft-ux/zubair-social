package com.zubair.social.auth

import kotlinx.coroutines.delay

/**
 * Login fail hone ki wajahein (01-users-auth.md ki table). `message` user ko dikhta hai.
 * Security tip: "email galat" aur "password galat" alag nahi batate, dono ke liye ek hi generic message
 * (attacker ko pata na chale ke email registered hai ya nahi).
 */
enum class AuthError(val message: String) {
    InvalidCredentials("Incorrect email or password"),            // wrong password + user not found
    InvalidEmail("Enter a valid email address"),
    UserDisabled("This account has been disabled"),
    TooManyRequests("Too many attempts. Please try again later"),
    Network("Check your internet connection and try again"),
    Unknown("Something went wrong. Please try again"),
}

sealed interface AuthResult {
    data object Success : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

/** ViewModel sirf is interface se baat karta hai, Firebase se nahi. Isse Firebase baad mein plug-in ho sakta hai. */
interface AuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult

    /** Sign up form mein live "available hai ya nahi" ke liye. null = check nahi ho saka (e.g. no internet). */
    suspend fun isUsernameAvailable(username: String): Boolean?

    /** Doc ke steps 6-10: username check -> Auth account -> /users aur /usernames write. */
    suspend fun signUp(email: String, fullName: String, username: String, password: String): SignUpResult
}

/** Sign up fail hone ki wajahein (01-users-auth.md ki failure table). */
enum class SignUpError(val message: String) {
    UsernameTaken("This username is already taken. Try another one"),
    EmailInUse("An account with this email already exists. Try logging in"),
    WeakPassword("Password must be at least 6 characters"),
    InvalidEmail("Enter a valid email address"),
    Network("Check your internet connection and try again"),
    Unknown("Something went wrong. Please try again"),
}

sealed interface SignUpResult {
    data object Success : SignUpResult
    data class Failure(val error: SignUpError) : SignUpResult
}

/**
 * Temporary: Firebase abhi connected nahi. UI test karne ke liye
 * test@zubair.com / 123456 se login hota hai, baqi sab InvalidCredentials.
 * Sign up: usernames zubair/admin/taken "taken" hain, used@zubair.com "already registered" hai.
 * Firebase connect hone par FirebaseAuthRepository (signInWithEmailAndPassword) is ki jagah aayega.
 */
class FakeAuthRepository : AuthRepository {
    override suspend fun signIn(email: String, password: String): AuthResult {
        delay(1000)
        return if (email == "test@zubair.com" && password == "123456") AuthResult.Success
        else AuthResult.Failure(AuthError.InvalidCredentials)
    }

    private val takenUsernames = setOf("zubair", "admin", "taken")

    override suspend fun isUsernameAvailable(username: String): Boolean? {
        delay(400)
        return username.lowercase() !in takenUsernames
    }

    override suspend fun signUp(
        email: String, fullName: String, username: String, password: String,
    ): SignUpResult {
        delay(1000)
        return when {
            username.lowercase() in takenUsernames -> SignUpResult.Failure(SignUpError.UsernameTaken)
            email == "used@zubair.com" -> SignUpResult.Failure(SignUpError.EmailInUse)
            else -> SignUpResult.Success
        }
    }
}

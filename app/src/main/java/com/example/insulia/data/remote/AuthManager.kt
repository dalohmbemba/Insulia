package com.example.insulia.data.remote

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

/**
 * Handles the patient's Firebase account.
 *
 * The app starts with anonymous authentication so a patient can use Insulia
 * immediately. Patients can later attach an email and password to the same
 * anonymous account (account linking). Linking keeps the existing UID, which
 * means every record under `users/{uid}` stays exactly where it is.
 *
 * The same credentials can be used on a new installation to sign back in and
 * recover the records that belong to that account.
 */
class AuthManager(
    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (_: Throwable) {
        null
    }
) {
    val isAvailable: Boolean get() = auth != null

    val isSignedIn: Boolean get() = auth?.currentUser != null

    /** `true` when the current session is still an anonymous session. */
    val isAnonymous: Boolean get() = auth?.currentUser?.isAnonymous ?: true

    val email: String? get() = auth?.currentUser?.email

    val uid: String? get() = auth?.currentUser?.uid

    /**
     * Makes sure a user exists before any Firestore work is attempted.
     */
    suspend fun ensureSignedIn(): String {
        val instance = auth ?: throw IllegalStateException("Firebase authentication is not available")
        val current = instance.currentUser
        if (current != null) return current.uid
        val result = instance.signInAnonymously().await()
        return result.user?.uid ?: throw IllegalStateException("Could not sign in anonymously")
    }

    /**
     * Adds an email/password sign-in method to the current anonymous account.
     *
     * The UID does not change, so the patient's existing records are preserved.
     */
    suspend fun linkWithEmail(email: String, password: String): Result<Unit> = runCatching {
        val instance = auth ?: throw IllegalStateException("Firebase authentication is not available")
        val user = instance.currentUser
            ?: throw IllegalStateException("No signed-in patient to upgrade")
        if (!user.isAnonymous) {
            throw IllegalStateException("This account already has an email sign-in method")
        }
        val credential = EmailAuthProvider.getCredential(email.trim(), password)
        user.linkWithCredential(credential).await()
        Unit
    }.mapError()

    /**
     * Signs in to an existing email/password account (for example after a
     * reinstall) so the patient regains access to their records.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<Unit> = runCatching {
        val instance = auth ?: throw IllegalStateException("Firebase authentication is not available")
        instance.signInWithEmailAndPassword(email.trim(), password).await()
        Unit
    }.mapError()

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        val instance = auth ?: throw IllegalStateException("Firebase authentication is not available")
        instance.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.mapError()

    /**
     * Turns Firebase's exception types into messages a patient can act on.
     */
    private fun Result<Unit>.mapError(): Result<Unit> = fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { throwable ->
            val message = when (throwable) {
                is FirebaseAuthWeakPasswordException ->
                    "Choose a password with at least 6 characters"

                is FirebaseAuthUserCollisionException ->
                    "That email is already used by another account"

                is FirebaseAuthInvalidCredentialsException ->
                    "Email or password is incorrect"

                is FirebaseAuthInvalidUserException ->
                    "No account exists for that email"

                else -> throwable.message ?: "Account operation failed"
            }
            Result.failure(IllegalStateException(message, throwable))
        }
    )
}

package com.nguyenmoclam.tutorialyoutubemadesimple.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

/**
 * Manager class for handling Google authentication
 */
@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private lateinit var googleSignInClient: GoogleSignInClient

    init {
        initGoogleSignIn()
    }

    /**
     * Initialize Google Sign-In client
     */
    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope("https://www.googleapis.com/auth/youtube.readonly"),
                Scope("https://www.googleapis.com/auth/youtube.force-ssl"),
                Scope("https://www.googleapis.com/auth/youtubepartner")
            )
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    /**
     * Check if user is currently signed in
     */
    fun isUserSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null
    }

    /**
     * Get the sign-in intent to launch with ActivityResultLauncher
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * Handle the sign-in result
     */
    fun handleSignInResult(task: Task<GoogleSignInAccount>): GoogleSignInAccount? {
        return try {
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            // Sign in failed
            null
        }
    }

    /**
     * Sign out the current user
     */
    suspend fun signOut() {
        return suspendCancellableCoroutine { continuation ->
            googleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(Unit) {}
                } else {
                    continuation.resumeWithException(task.exception ?: Exception("Sign out failed"))
                }
            }
        }
    }

    /**
     * Get the current signed-in account
     */
    fun getCurrentAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}
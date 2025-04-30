package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.nguyenmoclam.tutorialyoutubemadesimple.auth.AuthManager
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.FirestoreRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.FirestoreState
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Auth state data class to represent different states of authentication
 * Simplified to focus on loading/error, user data is in UserDataRepository
 */
data class AuthProcessState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTrialLoading: Boolean = false,
    val firestoreError: String? = null
)

/**
 * ViewModel to handle Firebase authentication and user state updates via UserDataRepository
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val authManager: AuthManager,
    private val firestoreRepository: FirestoreRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    // State for loading/error specific to AuthViewModel operations
    private val _processState = MutableStateFlow(AuthProcessState())
    val processState: StateFlow<AuthProcessState> = _processState.asStateFlow()

    // Expose user state from UserDataRepository
    val userStateFlow: StateFlow<FirebaseUser?> = userDataRepository.userStateFlow

    // Expose free calls state from UserDataRepository
    val freeCallsStateFlow: StateFlow<Int?> = userDataRepository.freeCallsStateFlow

    init {
        // Initial user check
        val currentUser = auth.currentUser
        userDataRepository.updateUser(currentUser)

        // Set up auth state listener to update the central repository
        auth.addAuthStateListener { firebaseAuth ->
            val newUser = firebaseAuth.currentUser
            userDataRepository.updateUser(newUser)
            _processState.value = _processState.value.copy(isLoading = false)

            // Fetch free calls when user changes
            newUser?.uid?.let { userId ->
                fetchFreeCallsRemaining(userId)
            }
        }

        // Monitor Firestore loading and error states
        viewModelScope.launch {
            firestoreRepository.isLoading.collectLatest { isLoading ->
                _processState.value = _processState.value.copy(isTrialLoading = isLoading)
            }
        }

        viewModelScope.launch {
            firestoreRepository.error.collectLatest { error ->
                _processState.value = _processState.value.copy(firestoreError = error)
            }
        }

        // Fetch free calls for initial user if logged in
        currentUser?.uid?.let { userId ->
            fetchFreeCallsRemaining(userId)
        }
    }

    /**
     * Get sign-in intent for Google authentication
     */
    fun signIn(): Intent? {
        _processState.value = AuthProcessState(isLoading = true) // Reset state, set loading
        return authManager.getSignInIntent()
    }

    /**
     * Handle the sign-in result and authenticate with Firebase
     */
    fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            try {
                val account = authManager.handleSignInResult(task)
                if (account != null) {
                    // Google Sign-In was successful, authenticate with Firebase
                    firebaseAuthWithGoogle(account)
                } else {
                    // Google Sign-In failed
                    _processState.value = AuthProcessState(isLoading = false, error = "Google Sign-In failed")
                }
            } catch (e: Exception) {
                _processState.value = AuthProcessState(isLoading = false, error = e.message)
            }
        }
    }

    /**
     * Authenticate with Firebase using Google credentials
     */
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        _processState.value = AuthProcessState(isLoading = true)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success - user state is updated by the listener
                    // Fetch/Grant free trial
                    auth.currentUser?.uid?.let { userId ->
                        checkAndGrantFreeTrial(userId)
                    }
                    // Reset process state (loading handled by listener)
                     _processState.value = AuthProcessState()
                } else {
                    // Sign in failed - update user state via repository (listener might not have fired yet)
                    userDataRepository.updateUser(null)
                    _processState.value = AuthProcessState(
                        isLoading = false,
                        error = task.exception?.message ?: "Authentication failed"
                    )
                }
            }
    }

    /**
     * Check and grant free trial for a user
     */
    private fun checkAndGrantFreeTrial(userId: String) {
        viewModelScope.launch {
            when (val result = firestoreRepository.checkAndGrantFreeTrial(userId)) {
                is FirestoreState.Success -> {
                    // After granting trial, fetch the free calls remaining
                    fetchFreeCallsRemaining(userId)
                }
                is FirestoreState.Error -> {
                    _processState.value = _processState.value.copy(firestoreError = result.message)
                }
                else -> { /* Loading state is handled via flow collection */ }
            }
        }
    }

    /**
     * Fetch the number of free calls remaining for a user and update repository
     */
    fun fetchFreeCallsRemaining(userId: String) {
        viewModelScope.launch {
            when (val result = firestoreRepository.getFreeCallsRemaining(userId)) {
                is FirestoreState.Success -> {
                    userDataRepository.updateFreeCalls(result.data)
                }
                is FirestoreState.Error -> {
                    _processState.value = _processState.value.copy(firestoreError = result.message)
                }
                else -> { /* Loading state is handled via flow collection */ }
            }
        }
    }

    /**
     * Decrement the number of free calls for a user via repository
     * Returns the new count or null if operation failed
     */
    suspend fun decrementFreeCall(userId: String): Int? {
        return when (val result = firestoreRepository.decrementFreeCall(userId)) {
            is FirestoreState.Success -> {
                userDataRepository.updateFreeCalls(result.data)
                result.data
            }
            is FirestoreState.Error -> {
                _processState.value = _processState.value.copy(firestoreError = result.message)
                null
            }
            else -> null // Loading state is handled via flow collection
        }
    }

    /**
     * Sign out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                _processState.value = AuthProcessState(isLoading = true)
                // Sign out from Firebase (listener will update repo user state)
                auth.signOut()
                // Also sign out from Google
                authManager.signOut()
                // Clear all data in the central repository
                userDataRepository.clearAllData()
                // Repository user state is updated via listener
                // Reset process state
                _processState.value = AuthProcessState()
            } catch (e: Exception) {
                // Ensure user state is cleared in repo even on error
                userDataRepository.clearAllData()
                _processState.value = AuthProcessState(isLoading = false, error = e.message)
            }
        }
    }

    /**
     * Clear Firestore error state
     */
    fun clearFirestoreError() {
        firestoreRepository.clearError()
        _processState.value = _processState.value.copy(firestoreError = null)
    }
} 
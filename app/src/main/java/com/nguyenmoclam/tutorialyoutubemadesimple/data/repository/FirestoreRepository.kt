package com.nguyenmoclam.tutorialyoutubemadesimple.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sealed class representing different states of Firestore operations
 */
sealed class FirestoreState<out T> {
    object Loading : FirestoreState<Nothing>()
    data class Success<T>(val data: T) : FirestoreState<T>()
    data class Error(val message: String, val exception: Exception? = null) : FirestoreState<Nothing>()
}

/**
 * Repository for handling Firestore operations related to the free trial functionality
 */
@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val FREE_CALLS_FIELD = "freeCallsRemaining"
        private const val TRIAL_TIMESTAMP_FIELD = "trialGivenTimestamp"
        private const val LAST_CALL_TIMESTAMP_FIELD = "lastApiCallTimestamp"
        private const val DEFAULT_FREE_CALLS = 10
    }
    
    // State flows for tracking operation status
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: Flow<String?> = _error.asStateFlow()
    
    /**
     * Check if the user has a free trial allocation and grant one if not
     * @param userId The Firebase user ID
     * @return FirestoreState containing a Boolean indicating if a new trial was granted
     */
    suspend fun checkAndGrantFreeTrial(userId: String): FirestoreState<Boolean> {
        _isLoading.value = true
        _error.value = null
        
        val userRef = firestore.collection(USERS_COLLECTION).document(userId)
        
        return try {
            // Use a transaction to ensure data consistency
            val result = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                
                if (!snapshot.exists()) {
                    // User document doesn't exist, create it with free trial
                    val userData = hashMapOf(
                        FREE_CALLS_FIELD to DEFAULT_FREE_CALLS,
                        TRIAL_TIMESTAMP_FIELD to com.google.firebase.Timestamp.now(),
                        LAST_CALL_TIMESTAMP_FIELD to null
                    )
                    transaction.set(userRef, userData)
                    return@runTransaction true
                } else if (!snapshot.contains(FREE_CALLS_FIELD)) {
                    // User exists but no free calls field, add it
                    transaction.update(
                        userRef, 
                        FREE_CALLS_FIELD, DEFAULT_FREE_CALLS,
                        TRIAL_TIMESTAMP_FIELD, com.google.firebase.Timestamp.now()
                    )
                    return@runTransaction true
                }
                
                // User already has a trial allocation
                return@runTransaction false
            }.await()
            
            _isLoading.value = false
            FirestoreState.Success(result)
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = "Failed to check/grant trial: ${e.message}"
            FirestoreState.Error("Failed to check/grant trial", e)
        }
    }
    
    /**
     * Get the number of free calls remaining for a user
     * @param userId The Firebase user ID
     * @return FirestoreState containing the number of free calls remaining
     */
    suspend fun getFreeCallsRemaining(userId: String): FirestoreState<Int?> {
        _isLoading.value = true
        _error.value = null
        
        return try {
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            _isLoading.value = false
            
            if (userDoc.exists() && userDoc.contains(FREE_CALLS_FIELD)) {
                FirestoreState.Success(userDoc.getLong(FREE_CALLS_FIELD)?.toInt())
            } else {
                FirestoreState.Success(null)
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = "Failed to get free calls: ${e.message}"
            FirestoreState.Error("Failed to get free calls", e)
        }
    }
    
    /**
     * Decrement the number of free calls for a user
     * @param userId The Firebase user ID
     * @return FirestoreState containing the new count after decrementing
     */
    suspend fun decrementFreeCall(userId: String): FirestoreState<Int?> {
        _isLoading.value = true
        _error.value = null
        
        val userRef = firestore.collection(USERS_COLLECTION).document(userId)
        
        return try {
            val result = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                
                if (snapshot.exists() && snapshot.contains(FREE_CALLS_FIELD)) {
                    val currentCalls = snapshot.getLong(FREE_CALLS_FIELD) ?: 0
                    
                    // Don't decrement below zero
                    val newCount = maxOf(0, currentCalls - 1).toInt()
                    
                    transaction.update(
                        userRef,
                        FREE_CALLS_FIELD, newCount,
                        LAST_CALL_TIMESTAMP_FIELD, com.google.firebase.Timestamp.now()
                    )
                    
                    return@runTransaction newCount
                }
                
                return@runTransaction null
            }.await()
            
            _isLoading.value = false
            FirestoreState.Success(result)
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = "Failed to decrement free calls: ${e.message}"
            FirestoreState.Error("Failed to decrement free calls", e)
        }
    }
    
    /**
     * Clear any error state
     */
    fun clearError() {
        _error.value = null
    }
} 
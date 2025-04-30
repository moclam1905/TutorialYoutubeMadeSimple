package com.nguyenmoclam.tutorialyoutubemadesimple.data.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository to hold and manage shared user authentication state and related data.
 */
@Singleton
class UserDataRepository @Inject constructor() {

    // --- User State ---
    private val _userStateFlow = MutableStateFlow<FirebaseUser?>(null)
    val userStateFlow: StateFlow<FirebaseUser?> = _userStateFlow.asStateFlow()

    fun updateUser(user: FirebaseUser?) {
        _userStateFlow.value = user
    }

    // --- Free Calls State ---
    private val _freeCallsStateFlow = MutableStateFlow<Int?>(null)
    val freeCallsStateFlow: StateFlow<Int?> = _freeCallsStateFlow.asStateFlow()

    fun updateFreeCalls(count: Int?) {
        _freeCallsStateFlow.value = count
    }

    // --- Loading/Error State (Optional - Can be added if needed globally) ---
    // Example:
    // private val _isLoading = MutableStateFlow(false)
    // val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    // fun setLoading(loading: Boolean) { _isLoading.value = loading }

    // private val _error = MutableStateFlow<String?>(null)
    // val error: StateFlow<String?> = _error.asStateFlow()
    // fun setError(errorMessage: String?) { _error.value = errorMessage }

    fun clearAllData() {
        _userStateFlow.value = null
        _freeCallsStateFlow.value = null
        // Clear other states if added
    }
} 
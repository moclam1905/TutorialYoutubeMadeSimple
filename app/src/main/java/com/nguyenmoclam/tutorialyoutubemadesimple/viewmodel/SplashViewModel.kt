package com.nguyenmoclam.tutorialyoutubemadesimple.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Splash Screen.
 * Handles data preloading and initialization tasks.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val networkUtils: NetworkUtils
) : ViewModel() {

    companion object {
        private const val MINIMUM_SPLASH_DURATION = 2000L // 2 seconds minimum display time
    }

    // UI state for the Splash screen
    private val _state = MutableStateFlow(SplashViewState())
    val state: StateFlow<SplashViewState> = _state

    init {
        // Start preloading data when ViewModel is initialized
        preloadData()
        // Monitor network connectivity
        monitorNetworkConnectivity()
    }

    /**
     * Monitors network connectivity changes and updates the UI state accordingly.
     */
    private fun monitorNetworkConnectivity() {
        viewModelScope.launch {
            networkUtils.observeNetworkConnectivity().collect { isAvailable ->
                _state.update { it.copy(networkAvailable = isAvailable) }
                if (isAvailable && state.value.error != null) {
                    // Retry initialization when network becomes available
                    retryInitialization()
                }
            }
        }
    }

    /**
     * Preloads essential data needed for the app.
     * This includes fetching quizzes from the database.
     */
    private fun preloadData() {
        viewModelScope.launch {
            try {
                // Set loading state
                _state.update { it.copy(isLoading = true, error = null) }

                // Start timing for minimum display duration
                val startTime = System.currentTimeMillis()
                
                // Check network connectivity
                if (!networkUtils.isNetworkAvailable()) {
                    // If no network, wait for minimum duration then update state
                    delay(MINIMUM_SPLASH_DURATION)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isInitialized = true, // Mark as initialized so splash screen disappears
                            networkAvailable = false,
                            error = "No internet connection available"
                        )
                    }
                    return@launch
                }

                // Calculate remaining time to meet minimum duration
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = (MINIMUM_SPLASH_DURATION - elapsedTime).coerceAtLeast(0)

                // Wait for remaining time if needed
                if (remainingTime > 0) {
                    delay(remainingTime)
                }

                // Update state to indicate initialization is complete
                _state.update {
                    it.copy(
                        isLoading = false,
                        isInitialized = true,
                        networkAvailable = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                // Handle any errors during preloading
                _state.update {
                    it.copy(
                        isLoading = false,
                        isInitialized = true, // Mark as initialized so splash screen disappears
                        error = e.message ?: "An error occurred during initialization",
                        networkAvailable = networkUtils.isNetworkAvailable()
                    )
                }
                // Ensure minimum display time even in error case
                delay(MINIMUM_SPLASH_DURATION)
            }
        }
    }

    /**
     * Retry preloading data if it failed.
     */
    fun retryInitialization() {
        preloadData()
    }
}

data class SplashViewState(
    val isLoading: Boolean = true,
    val isInitialized: Boolean = false,
    val error: String? = null,
    val networkAvailable: Boolean = false
)
package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global network state listener for the application.
 * Provides information about current connection status and notifies when changes occur.
 * Uses StateFlow to emit network state change events.
 */
@Singleton
class NetworkStateListener @Inject constructor(
    context: Context,
    private val offlineSyncManager: OfflineSyncManager
) {
    companion object {
        private const val TAG = "NetworkStateListener"
    }

    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // StateFlow to monitor network state
    private val _networkState = MutableStateFlow(NetworkState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()
    
    // Callback to listen for network change events
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val isValidated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
            val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            val isMobile = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
            
            val previousState = _networkState.value
            val newState = NetworkState(
                isConnected = true,
                isValidated = isValidated,
                isWifi = isWifi,
                isMobile = isMobile,
                isReconnected = !previousState.isConnected
            )
            
            _networkState.value = newState
            
            if (newState.isReconnected) {
                Log.d(TAG, "Network connection restored: $newState")
                handleNetworkReconnection()
            }
        }

        override fun onLost(network: Network) {
            _networkState.value = NetworkState(
                isConnected = false,
                isValidated = false,
                isWifi = false,
                isMobile = false,
                isReconnected = false
            )
            Log.d(TAG, "Network connection lost")
            handleNetworkDisconnection()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val isMobile = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            
            _networkState.value = _networkState.value.copy(
                isValidated = isValidated,
                isWifi = isWifi,
                isMobile = isMobile
            )
        }
    }

    init {
        registerNetworkCallback()
        // Initialize initial network state
        updateCurrentNetworkState()
    }

    /**
     * Register callback to listen for network change events
     */
    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try {
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Could not register NetworkCallback", e)
        }
    }

    /**
     * Unregister callback when no longer needed
     */
    fun unregisterNetworkCallback() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            Log.d(TAG, "Successfully unregistered NetworkCallback")
        } catch (e: Exception) {
            Log.e(TAG, "Could not unregister NetworkCallback", e)
        } finally {
            // Ensure sync tasks are stopped even if there's an error
            try {
                offlineSyncManager.stopObservingNetworkChanges()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping sync tasks: ${e.message}")
            }
        }
    }

    /**
     * Update current network state
     */
    private fun updateCurrentNetworkState() {
        val network = connectivityManager.activeNetwork
        val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
        
        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val isValidated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isMobile = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        
        _networkState.value = NetworkState(
            isConnected = isConnected,
            isValidated = isValidated,
            isWifi = isWifi,
            isMobile = isMobile,
            isReconnected = false
        )
    }

    /**
     * Handle network reconnection
     * Automatically handle transition from offline to online mode and sync data
     */
    private fun handleNetworkReconnection() {
        Log.d(TAG, "Network connection restored, starting sync process")
        
        // Check connection quality before taking actions
        val networkState = _networkState.value
        if (!networkState.isValidated) {
            Log.d(TAG, "Network connection not validated, waiting for validation before sync")
            return
        }
        
        // Start data synchronization in background with coroutine
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Automatically sync data when network connection is restored
                // Add delay to ensure connection is stable before syncing
                delay(5000)
                
                // Recheck connection before starting sync
                if (_networkState.value.isConnected && _networkState.value.isValidated) {
                    Log.d(TAG, "Starting data sync after connection restored")
                    offlineSyncManager.startSync()
                } else {
                    Log.d(TAG, "Connection unstable, skipping data sync")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing data after reconnection: ${e.message}")
            }
        }
    }

    /**
     * Handle network disconnection
     * Application will automatically use cached data when network is lost
     * No longer switches to offline mode as app always works with cached data when no network
     */
    private fun handleNetworkDisconnection() {
        Log.d(TAG, "Network lost, automatically using cached data")
        
        // No need to call setOfflineModeEnabled as app always works with cached data
        // when there's no network connection, no need to switch modes
        
        // Stop running sync tasks (if any)
        coroutineScope.launch {
            try {
                // Notify OfflineSyncManager to stop sync tasks
                offlineSyncManager.stopObservingNetworkChanges()
                Log.d(TAG, "Stopped sync tasks due to network loss")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping sync tasks: ${e.message}")
            }
        }
    }
}

/**
 * Data class representing network state
 */
data class NetworkState(
    val isConnected: Boolean = false,
    val isValidated: Boolean = false,
    val isWifi: Boolean = false,
    val isMobile: Boolean = false,
    val isReconnected: Boolean = false
)
package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient.Builder
import okhttp3.Response
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Utility class for handling network connectivity checks and monitoring.
 * Supports offline mode and network state tracking.
 */
class NetworkUtils(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val preferences: SharedPreferences = 
        context.getSharedPreferences(NETWORK_PREFERENCES, Context.MODE_PRIVATE)

    // Data saver mode settings
    private var dataSaverEnabled = false
    private var connectionTypeRestriction = "any" // "any", "wifi_only" or "mobile_only"
    private var connectionTimeout = 120 // Default timeout in seconds
    private var retryPolicy = "exponential" // "none", "linear", or "exponential"
    
    /**
     * Check if the device currently has an active internet connection.
     */
    fun isNetworkAvailable(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Check if the current network is WiFi
     */
    fun isWifiConnection(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Check if the current network is mobile data
     */
    fun isMobileDataConnection(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Check if the current active network is metered.
     */
    fun isMeteredNetwork(): Boolean {
        return try {
            connectivityManager.isActiveNetworkMetered
        } catch (e: SecurityException) {
            // Assume metered if we can't check due to permissions
            true 
        }
    }

    /**
     * Determines if content should be loaded when on a metered network.
     * Uses user preferences to decide whether to allow content loading on metered networks.
     */
    fun shouldLoadContentOnMetered(): Boolean {
        return preferences.getBoolean(KEY_ALLOW_METERED_NETWORK, DEFAULT_ALLOW_METERED_NETWORK)
    }

    /**
     * Sets whether content should be loaded on metered networks
     * 
     * @param allowed true to allow loading on metered networks, false otherwise
     */
    fun setAllowContentOnMetered(allowed: Boolean) {
        preferences.edit().putBoolean(KEY_ALLOW_METERED_NETWORK, allowed).apply()
    }

    /**
     * Observe network connectivity changes as a Flow.
     * @return Flow<Boolean> that emits true when connected, false when disconnected
     */
    fun observeNetworkConnectivity(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ) {
                    trySend(true)
                }
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                val hasInternet =
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(hasInternet)
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Send initial value
        trySend(isNetworkAvailable())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    /**
     * Set data saver mode enabled/disabled
     */
    fun setDataSaverEnabled(enabled: Boolean) {
        dataSaverEnabled = enabled
    }

    /**
     * Set connection type restriction
     * @param type "any" or "wifi_only"
     */
    fun setConnectionTypeRestriction(type: String) {
        connectionTypeRestriction = type
    }

    /**
     * Get current data saver mode state
     */
    fun isDataSaverEnabled(): Boolean {
        return dataSaverEnabled
    }

    /**
     * Get current connection type restriction
     */
    fun getConnectionTypeRestriction(): String {
        return connectionTypeRestriction
    }

    /**
     * Check if content should be loaded based on network settings
     * This method has been updated to always allow displaying cached content when there is no network connection
     * Performs atomic check to avoid race conditions
     */
    @Synchronized
    fun shouldLoadContent(highQuality: Boolean = false): Boolean {
        val isNetworkAvailable = isNetworkAvailable()
        
        // If network is not available, always allow displaying cached content
        // Return true so the app can work in offline mode
        if (!isNetworkAvailable) {
            return true
        }
        
        // Check connection type restrictions when network is available
        if (connectionTypeRestriction == "wifi_only" && !isWifiConnection()) {
            return false
        }

        if (connectionTypeRestriction == "mobile_only" && !isMobileDataConnection()) {
            return false
        }

        // Check data saver mode for high quality content
        if (highQuality && dataSaverEnabled) {
            return false
        }

        return true
    }

    /**
     * Get recommended image quality based on data saver settings
     * @return Quality level: "high", "medium", or "low"
     */
    fun getRecommendedImageQuality(): String {
        if (!isNetworkAvailable()) return "low"

        if (isWifiConnection() && !dataSaverEnabled) {
            return "high"
        }

        if (dataSaverEnabled) {
            return "low"
        }

        return "medium"
    }

    /**
     * Get recommended video quality based on data saver settings
     * @return Quality level: "high", "medium", or "low"
     */
    fun getRecommendedVideoQuality(): String {
        if (!isNetworkAvailable()) return "low"

        if (isWifiConnection() && !dataSaverEnabled) {
            return "high"
        }

        if (dataSaverEnabled) {
            return "low"
        }

        return "medium"
    }

    /**
     * Set connection timeout in seconds
     * @param seconds Timeout value in seconds
     */
    fun setConnectionTimeout(seconds: Int) {
        connectionTimeout = seconds
    }

    /**
     * Get current connection timeout in seconds
     * @return Timeout value in seconds
     */
    fun getConnectionTimeout(): Int {
        return connectionTimeout
    }

    /**
     * Execute a network operation with a timeout.
     * Applies the configured timeout value to the operation.
     *
     * @param block The suspend function to execute with timeout
     * @return The result of the operation
     */
    suspend fun <T> withConnectionTimeout(block: suspend () -> T): T {
        return try {
            // Use the configured timeout value from settings (in seconds)
            withTimeout(connectionTimeout * 1000L) {
                block()
            }
        } catch (e: TimeoutCancellationException) {
            throw SocketTimeoutException("Connection timed out after $connectionTimeout seconds")
        }
    }

    /**
     * Configure OkHttpClient with the current connection timeout settings
     * This method should be used when creating OkHttpClient instances
     *
     * @param builder The OkHttpClient.Builder to configure
     * @return The configured OkHttpClient.Builder
     */
    fun configureOkHttpClient(builder: Builder): Builder {
        val configuredBuilder = builder
            .connectTimeout(connectionTimeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(connectionTimeout.toLong(), TimeUnit.SECONDS)
            .writeTimeout(connectionTimeout.toLong(), TimeUnit.SECONDS)

        // Apply retry policy if not set to "none"
        //The NetworkUtils class properly handles different retry policies (none, linear, exponential) with appropriate retry counts and backoff strategies:
        //- For linear policy: 3 retries with delays of 1s, 2s, and 3s
        //- For exponential policy: 5 retries with delays of 1s, 2s, 4s, 8s, and 16s
        //- For none policy: No retries are attempted
        if (retryPolicy != "none") {
            configuredBuilder.addInterceptor { chain ->
                var request = chain.request()
                var response: Response? = null
                var exception: Exception? = null

                val maxRetries = when (retryPolicy) {
                    "linear" -> 3
                    "exponential" -> 5
                    else -> 0
                }

                var retryCount = 0

                while (retryCount <= maxRetries) {
                    try {
                        // If this isn't the first attempt, create a new request
                        if (retryCount > 0) {
                            request = request.newBuilder().build()
                        }

                        response = chain.proceed(request)

                        // If response is successful, return it
                        if (response.isSuccessful) {
                            return@addInterceptor response
                        } else if (response.code in listOf(408, 429, 500, 502, 503, 504)) {
                            // Server errors that are worth retrying
                            response.close()
                        } else {
                            // Client errors or other server errors that shouldn't be retried
                            return@addInterceptor response
                        }
                    } catch (e: Exception) {
                        exception = e
                    }

                    retryCount++

                    if (retryCount <= maxRetries) {
                        // Calculate delay based on retry policy
                        val delayMillis = when (retryPolicy) {
                            "linear" -> 1000L * retryCount // Linear: 1s, 2s, 3s
                            "exponential" -> 1000L * (1 shl (retryCount - 1)) // Exponential: 1s, 2s, 4s, 8s, 16s
                            else -> 0L
                        }

                        if (delayMillis > 0) {
                            Thread.sleep(delayMillis)
                        }
                    }
                }

                // If we got here, we've exhausted our retries
                if (response != null) {
                    return@addInterceptor response
                } else if (exception != null) {
                    throw exception
                } else {
                    throw java.io.IOException("Retry policy exhausted")
                }
            }
        }

        return configuredBuilder
    }

    /**
     * Set retry policy
     * @param policy The retry policy ("none", "linear", or "exponential")
     */
    fun setRetryPolicy(policy: String) {
        retryPolicy = policy
    }

    /**
     * Get current retry policy
     * @return The current retry policy
     */
    fun getRetryPolicy(): String {
        return retryPolicy
    }

    companion object {
        // Preference keys
        private const val NETWORK_PREFERENCES = "network_preferences"
        private const val KEY_ALLOW_METERED_NETWORK = "allow_metered_network"
        
        // Default values
        private const val DEFAULT_ALLOW_METERED_NETWORK = false
    }
}
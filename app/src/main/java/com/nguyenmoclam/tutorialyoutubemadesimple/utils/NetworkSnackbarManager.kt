package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Manages the display of Snackbar notifications when network status changes.
 * This class is used throughout the application to show notifications when network connection is restored or lost.
 */
object NetworkSnackbarManager {

    /**
     * Composable to monitor network status and display Snackbar notifications when changes occur.
     *
     * @param snackbarHostState SnackbarHostState to display notifications
     * @param networkStateListener Listener to monitor network status
     * @param showReconnectionMessage Whether to show notification when network connection is restored
     * @param showDisconnectionMessage Whether to show notification when network connection is lost
     */
    @Composable
    fun NetworkStatusSnackbar(
        snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
        networkStateListener: NetworkStateListener = LocalNetworkStateListener.current,
        showReconnectionMessage: Boolean = true,
        showDisconnectionMessage: Boolean = true
    ) {
        val networkState by networkStateListener.networkState.collectAsState()

        // Message when network connection is restored
        val reconnectionMessage = stringResource(R.string.network_reconnected)

        // Message when network connection is lost
        val disconnectionMessage = stringResource(R.string.no_internet_connection)

        // Monitor network status changes
        LaunchedEffect(Unit) {
            networkStateListener.networkState
                .map { it.isConnected }
                .distinctUntilChanged()
                .collect { isConnected ->
                    if (isConnected && showReconnectionMessage && networkState.isReconnected) {
                        // Show notification when network connection is restored
                        // Requirement #4: When starting with no network, then getting network during usage
                        snackbarHostState.showSnackbar(
                            message = reconnectionMessage,
                            duration = SnackbarDuration.Short
                        )
                    } else if (!isConnected && showDisconnectionMessage) {
                        // Show notification when network connection is lost
                        // Requirement #3: When starting with network, then losing network during usage
                        snackbarHostState.showSnackbar(
                            message = disconnectionMessage,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
        }
    }
}
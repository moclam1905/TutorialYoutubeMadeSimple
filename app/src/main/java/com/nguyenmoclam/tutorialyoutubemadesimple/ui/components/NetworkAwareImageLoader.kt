package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import java.io.File

/**
 * A network-aware image loader component that respects network connectivity settings
 * and prioritizes loading from a local path if available.
 */
@Composable
fun NetworkAwareImageLoader(
    modifier: Modifier = Modifier,
    imageUrl: String, // Network URL (fallback)
    localPath: String? = null, // Local file path (priority)
    contentDescription: String? = null,
    networkUtils: NetworkUtils,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: @Composable (() -> Unit)? = null,
    onRetryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val shouldLoadContentFromNetwork = remember { networkUtils.shouldLoadContent() }
    val imageQuality = remember { networkUtils.getRecommendedImageQuality() }
    val connectionType = remember { networkUtils.getConnectionTypeRestriction() }

    // Determine the data source: Prioritize local path, fallback to network URL if allowed
    val imageDataSource: Any? = remember(localPath, imageUrl, shouldLoadContentFromNetwork) {
        if (!localPath.isNullOrBlank() && File(localPath).exists()) {
             // Use local file if path is valid and file exists
            File(localPath)
        } else if (shouldLoadContentFromNetwork && imageUrl.isNotBlank()) {
            // Fallback to network URL if allowed and URL is valid
            // Optimize network URL based on quality settings
            when (imageQuality) {
                "low" -> imageUrl.replace("maxresdefault", "mqdefault").replace("hqdefault", "mqdefault")
                "medium" -> imageUrl.replace("maxresdefault", "hqdefault")
                else -> imageUrl // high or default
            }
        } else {
            // No valid source available (offline without local file, or network restricted)
            null
        }
    }

    Box(modifier = modifier) {
        if (imageDataSource == null) {
            // No valid source: Show appropriate message or placeholder
            Column(
                modifier = Modifier
                    .fillMaxSize() // Fill the box to center content
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (!networkUtils.isNetworkAvailable()) Icons.Default.SignalWifiOff else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (!networkUtils.isNetworkAvailable()) {
                        // Offline and no local file found
                        stringResource(R.string.no_internet_connection) // Use existing string
                    } else if (connectionType == "wifi_only" && !networkUtils.isWifiConnection()) {
                        stringResource(R.string.wifi_only_mode_enabled)
                    } else if (connectionType == "mobile_only" && !networkUtils.isMobileDataConnection()) {
                        stringResource(R.string.mobile_data_only_mode_enabled)
                    } else if (networkUtils.isDataSaverEnabled()) {
                         // Data saver might prevent network load if local file failed
                        stringResource(R.string.data_saver_mode_enabled)
                    } else {
                         // Generic fallback if network is available but URL is blank or other issue
                        stringResource(R.string.image_load_error)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                // Show retry only if network seems available but loading failed (e.g., restriction)
                if (networkUtils.isNetworkAvailable()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetryClick) {
                        Text(stringResource(R.string.retry_button))
                    }
                }
            }
        } else {
            // Valid source: Load image with Coil
            var isLoading by remember { mutableStateOf(true) }
            var isError by remember { mutableStateOf(false) }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageDataSource) // Use the determined source (File or String URL)
                    .crossfade(true)
                    // Disk cache is useful even when loading from local file for transformations, etc.
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED) // Always use memory cache
                    .size(Size.ORIGINAL) // Add size hint (Coil might infer this anyway)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(), // Image fills the Box
                onState = { state ->
                    isLoading = state is AsyncImagePainter.State.Loading
                    isError = state is AsyncImagePainter.State.Error
                }
            )

            // Show placeholder while loading
            if (isLoading && placeholder != null) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    placeholder()
                 }
            } else if (isLoading) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                 }
            }


            // Show error state overlay if loading failed
            if (isError) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.image_load_error),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetryClick,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text(stringResource(R.string.retry_button))
                    }
                }
            }
        }
    }
}

package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

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
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils

/**
 * A network-aware image loader component that respects network connectivity settings.
 * This component checks network settings before loading images and displays
 * appropriate messages when images cannot be loaded due to network restrictions.
 */
@Composable
fun NetworkAwareImageLoader(
    modifier: Modifier = Modifier,
    imageUrl: String,
    contentDescription: String? = null,
    networkUtils: NetworkUtils,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: @Composable (() -> Unit)? = null,
    onRetryClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Check if content should be loaded based on network settings
    val shouldLoadContent = remember { networkUtils.shouldLoadContent() }
    val imageQuality = remember { networkUtils.getRecommendedImageQuality() }
    val isDataSaverEnabled = remember { networkUtils.isDataSaverEnabled() }
    val connectionType = remember { networkUtils.getConnectionTypeRestriction() }

    // If content should not be loaded due to network settings, show appropriate message
    if (!shouldLoadContent) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (!networkUtils.isNetworkAvailable()) {
                    Icons.Default.SignalWifiOff
                } else {
                    Icons.Default.CloudOff
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (!networkUtils.isNetworkAvailable()) {
                    stringResource(R.string.no_internet_connection)
                } else if (connectionType == "wifi_only" && !networkUtils.isWifiConnection()) {
                    stringResource(R.string.wifi_only_mode_enabled)
                } else if (connectionType == "mobile_only" && !networkUtils.isMobileDataConnection()) {
                    stringResource(R.string.mobile_data_only_mode_enabled)
                } else {
                    stringResource(R.string.data_saver_mode_enabled)
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRetryClick,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(stringResource(R.string.retry_button))
            }
        }
        return
    }

    // Adjust image URL based on recommended quality
    val optimizedImageUrl = when (imageQuality) {
        "low" -> imageUrl.replace("maxresdefault", "mqdefault")
            .replace("hqdefault", "mqdefault")

        "medium" -> imageUrl.replace("maxresdefault", "hqdefault")
        else -> imageUrl // Keep high quality if network is good
    }

    // Load image with Coil
    Box(modifier = modifier) {
        var isLoading by remember { mutableStateOf(true) }
        var isError by remember { mutableStateOf(false) }

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(optimizedImageUrl)
                .crossfade(true)
                .diskCachePolicy(if (isDataSaverEnabled) CachePolicy.ENABLED else CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.ENABLED) // Always use memory cache
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
            onState = { state ->
                isLoading = state is AsyncImagePainter.State.Loading
                isError = state is AsyncImagePainter.State.Error
            }
        )

        // Show placeholder while loading
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (placeholder != null) {
                    placeholder()
                } else {
                    CircularProgressIndicator()
                }
            }
        }

        // Show error state
        if (isError) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
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
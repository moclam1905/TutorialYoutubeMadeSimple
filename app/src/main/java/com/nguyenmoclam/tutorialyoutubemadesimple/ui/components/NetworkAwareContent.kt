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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.OfflineDataManager

/**
 * A smart component that can display content based on network state.
 * This component checks network settings before loading content and displays
 * appropriate messages when content cannot be loaded due to network restrictions.
 * 
 * When there is no network connection, the component will try to display content from local storage if available.
 */
@Composable
fun NetworkAwareContent(
    modifier: Modifier = Modifier,
    quizId: Long,
    networkUtils: NetworkUtils,
    offlineDataManager: OfflineDataManager,
    contentType: ContentType,
    onlineContent: @Composable () -> Unit,
    onRetryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    // coroutineScope variable has been removed as it is not used
    
    // State
    var isLoading by remember { mutableStateOf(true) }
    var offlineContentAvailable by remember { mutableStateOf(false) }
    var offlineContent by remember { mutableStateOf<String?>(null) }
    
    // Check if content is available offline
    LaunchedEffect(quizId, contentType) {
        isLoading = true
        offlineContentAvailable = when (contentType) {
            ContentType.SUMMARY -> offlineDataManager.isSummaryAvailableOffline(quizId)
            ContentType.MINDMAP -> offlineDataManager.isMindMapAvailableOffline(quizId)
        }
        
        if (offlineContentAvailable) {
            offlineContent = when (contentType) {
                ContentType.SUMMARY -> offlineDataManager.getSummaryHtml(quizId)
                ContentType.MINDMAP -> offlineDataManager.getMindMapSvg(quizId)
            }
        }
        
        isLoading = false
    }
    
    // Check if content should be loaded based on network settings
    val shouldLoadContent = remember { networkUtils.shouldLoadContent() }
    
    if (isLoading) {
        // Display loading state
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (!shouldLoadContent && !offlineContentAvailable) {
        // If there is no network connection and no offline content, display appropriate message
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
                } else {
                    val connectionType = networkUtils.getConnectionTypeRestriction()
                    when {
                        connectionType == "wifi_only" && !networkUtils.isWifiConnection() ->
                            stringResource(R.string.wifi_only_mode_enabled)
                        connectionType == "mobile_only" && !networkUtils.isMobileDataConnection() ->
                            stringResource(R.string.mobile_data_only_mode_enabled)
                        else -> stringResource(R.string.data_saver_mode_enabled)
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (contentType == ContentType.SUMMARY) {
                    stringResource(R.string.summary_not_available_offline)
                } else {
                    stringResource(R.string.mindmap_not_available_offline)
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetryClick,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(stringResource(R.string.retry_button))
            }
        }
    } else if (!shouldLoadContent && offlineContentAvailable) {
        // If there is no network connection but offline content is available, display offline content
        when (contentType) {
            ContentType.SUMMARY -> {
                offlineContent?.let { htmlContent ->
                    OfflineHtmlContent(
                        modifier = modifier,
                        htmlContent = htmlContent,
                        offlineDataManager = offlineDataManager
                    )
                }
            }
            ContentType.MINDMAP -> {
                offlineContent?.let { svgContent ->
                    OfflineSvgContent(
                        modifier = modifier,
                        svgContent = svgContent
                    )
                }
            }
        }
    } else {
        // If there is network connection, display online content
        onlineContent()
    }
}

/**
 * Display offline HTML content
 */
@Composable
fun OfflineHtmlContent(
    modifier: Modifier = Modifier,
    htmlContent: String,
    offlineDataManager: OfflineDataManager
) {
    // Use WebView but in offline mode
    AndroidWebViewWithOfflineContent(
        modifier = modifier,
        htmlContent = htmlContent,
        offlineDataManager = offlineDataManager
    )
}

/**
 * Display offline SVG content
 */
@Composable
fun OfflineSvgContent(
    modifier: Modifier = Modifier,
    svgContent: String
) {
    // Use SVGView to display SVG content
    AndroidSVGViewWithOfflineContent(
        modifier = modifier,
        svgContent = svgContent
    )
}

/**
 * Loại nội dung để hiển thị
 */
enum class ContentType {
    SUMMARY,
    MINDMAP
}
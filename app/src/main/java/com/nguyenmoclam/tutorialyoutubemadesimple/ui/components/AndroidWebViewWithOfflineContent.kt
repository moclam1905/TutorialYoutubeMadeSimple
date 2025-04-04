package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.LocalNetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.OfflineDataManager

/**
 * Component to display HTML content in WebView in offline mode.
 * Does not make any network requests and only displays stored HTML content.
 * Uses WebViewClientWithOfflineSupport to handle web requests in offline mode.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AndroidWebViewWithOfflineContent(
    modifier: Modifier = Modifier,
    htmlContent: String,
    offlineDataManager: OfflineDataManager,
    isJavaScriptEnabled: Boolean = true
) {
    val context = LocalContext.current
    val networkUtils = LocalNetworkUtils.current
    
    // State
    var isLoading by remember { mutableStateOf(true) }
    
    // Hiển thị WebView với nội dung HTML
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { 
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = isJavaScriptEnabled
                        domStorageEnabled = true
                        // Configure WebView for offline mode
                        cacheMode = WebSettings.LOAD_CACHE_ONLY
                        blockNetworkImage = false // Allow loading images from local storage
                        blockNetworkLoads = true // Block network loads
                        // Performance optimization
                        setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                        // Allow loading resources from files
                        allowFileAccess = true
                        allowContentAccess = true
                    }
                    
                    // Set up WebViewClient with offline support
                    webViewClient = WebViewClientWithOfflineSupport(
                        offlineDataManager = offlineDataManager,
                        networkUtils = networkUtils,
                        onPageStarted = { isLoading = true },
                        onPageFinished = { isLoading = false },
                        onReceivedError = { _, _ -> isLoading = false }
                    )
                }
            },
            update = { view ->
                // Load HTML content directly from string, with baseUrl to support relative resources
                view.loadDataWithBaseURL(
                    "file:///android_asset/",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        )
        
        // Display loading state
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
    
    // Cleanup when component is destroyed
    DisposableEffect(Unit) {
        onDispose {
            // No need to call destroy() as AndroidView will handle it automatically
        }
    }
}
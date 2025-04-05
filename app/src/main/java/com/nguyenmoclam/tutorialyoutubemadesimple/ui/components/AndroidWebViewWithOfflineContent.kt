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
 * Helper function to modify HTML content for optimal display in a WebView,
 * especially fixing image styles and adding viewport meta tag.
 */
private fun modifyHtmlForOfflineWebView(htmlContent: String): String {
    val imgTagRegex = Regex("""<img\s+([^>]*?)>""", RegexOption.IGNORE_CASE)
    val styleAttributeRegex = Regex("""\sstyle\s*=\s*['"][^'"]*['"]""", RegexOption.IGNORE_CASE)
    val widthAttributeRegex = Regex("""\swidth\s*=\s*['"]?\d+['"]?""", RegexOption.IGNORE_CASE)
    val heightAttributeRegex = Regex("""\sheight\s*=\s*['"]?\d+['"]?""", RegexOption.IGNORE_CASE)

    val modifiedHtml = imgTagRegex.replace(htmlContent) { matchResult ->
        var attributes = matchResult.groupValues[1]
        // Remove existing style, width, height attributes
        attributes = styleAttributeRegex.replace(attributes, "")
        attributes = widthAttributeRegex.replace(attributes, "")
        attributes = heightAttributeRegex.replace(attributes, "")
        // Ensure attributes string starts with a space if not empty, and add style *before* closing >
        val attributesPrefix =
            if (attributes.isNotBlank() && !attributes.startsWith(" ")) " " else ""
        """<img${attributesPrefix}${attributes.trim()} style="max-width: 100%; height: auto; display: block;">"""
    }

    // Prepend viewport meta tag
    val viewportMeta = "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
    return viewportMeta + modifiedHtml
}


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
                        // Improve scrolling and viewport handling
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false // Hide zoom buttons, allow pinch-to-zoom
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
                // Modify the HTML content before loading
                val finalHtml = modifyHtmlForOfflineWebView(htmlContent)
                // Load the modified HTML content directly from string, with baseUrl to support relative resources
                view.loadDataWithBaseURL(
                    "file:///android_asset/", // Using asset base URL might help with relative paths if needed
                    finalHtml,
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

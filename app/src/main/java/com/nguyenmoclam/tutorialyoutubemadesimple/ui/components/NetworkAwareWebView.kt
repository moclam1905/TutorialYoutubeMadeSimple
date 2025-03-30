package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils

/**
 * A WebView component that is aware of network connectivity settings.
 * This component checks network settings before loading content and displays
 * appropriate messages when content cannot be loaded due to network restrictions.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NetworkAwareWebView(
    modifier: Modifier = Modifier,
    url: String,
    html: String = "",
    networkUtils: NetworkUtils,
    isJavaScriptEnabled: Boolean = true,
    onPageFinished: () -> Unit = {},
    onRetryClick: () -> Unit = {},
    jsInterface: ((WebView) -> Unit)? = null
) {
    // State variables
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Check if content should be loaded based on network settings
    val shouldLoadContent = remember { networkUtils.shouldLoadContent() }
    val connectionType = remember { networkUtils.getConnectionTypeRestriction() }
    val isDataSaverEnabled = remember { networkUtils.isDataSaverEnabled() }

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
                modifier = Modifier.height(48.dp)
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

            Text(
                text = stringResource(R.string.change_network_settings),
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
        return
    }

    // If content should be loaded, show WebView
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = isJavaScriptEnabled
                        domStorageEnabled = true
                        loadsImagesAutomatically =
                            !isDataSaverEnabled // Disable image loading in data saver mode
                        blockNetworkImage = isDataSaverEnabled // Block images in data saver mode
                        cacheMode = if (isDataSaverEnabled) {
                            WebSettings.LOAD_CACHE_ELSE_NETWORK // Prefer cache in data saver mode
                        } else {
                            WebSettings.LOAD_DEFAULT
                        }
                        // Enable WebView to use hardware acceleration
                        setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                        // Enable fullscreen support
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        // Enable content access
                        allowContentAccess = true
                        // Enable file access
                        allowFileAccess = true
                        // Improve scrolling performance
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        // Optimize rendering
                        setRenderPriority(WebSettings.RenderPriority.HIGH)
                        // Enable hardware acceleration for SVG rendering
                        setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                        // Enable SVG support
                        mediaPlaybackRequiresUserGesture = false
                    }

                    // Improve scrolling performance
                    scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
                    isScrollbarFadingEnabled = true
                    isVerticalScrollBarEnabled = true
                    setOnLongClickListener { true } // Disable text selection long press

                    // Set layout parameters to ensure WebView takes full height
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Add WebChromeClient to handle fullscreen requests
                    webChromeClient = object : WebChromeClient() {
                        private var customView: View? = null
                        private var customViewCallback: CustomViewCallback? = null
                        private var originalSystemUiVisibility: Int = 0

                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            // This is called when the user enters fullscreen mode via HTML5 video or other content
                            if (customView != null) {
                                onHideCustomView()
                                return
                            }

                            customView = view
                            customViewCallback = callback

                            // Find the root view to add our fullscreen view to
                            val decorView =
                                (context as? Activity)?.window?.decorView as? ViewGroup
                            decorView?.let {
                                // Save original UI visibility
                                originalSystemUiVisibility = decorView.systemUiVisibility

                                // Add the custom view to the window
                                it.addView(
                                    customView,
                                    ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                )

                                // Set fullscreen flags
                                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            }
                        }

                        override fun onHideCustomView() {
                            // This is called when the user exits fullscreen mode
                            val decorView =
                                (context as? Activity)?.window?.decorView as? ViewGroup
                            decorView?.let {
                                // Remove the custom view
                                customView?.let { view -> it.removeView(view) }

                                // Restore original UI visibility
                                decorView.systemUiVisibility = originalSystemUiVisibility
                            }

                            customView = null
                            customViewCallback?.onCustomViewHidden()
                            customViewCallback = null
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            hasError = false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            onPageFinished()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                hasError = true
                                errorMessage = "Error loading page: ${error?.description}"
                                isLoading = false
                            }
                        }

                        override fun onReceivedHttpError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            errorResponse: WebResourceResponse?
                        ) {
                            super.onReceivedHttpError(view, request, errorResponse)
                            if (request?.isForMainFrame == true) {
                                hasError = true
                                errorMessage = "HTTP Error: ${errorResponse?.statusCode}"
                                isLoading = false
                            }
                        }
                    }

                    // Add JavaScript interface if provided
                    jsInterface?.invoke(this)

                    // Load content
                    if (html.isNotEmpty()) {
                        loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                    } else if (url.isNotEmpty()) {
                        loadUrl(url)
                    }
                }
            },
            update = { webView ->
                // Update WebView if needed
                if (html.isNotEmpty()) {
                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                } else if (url.isNotEmpty() && webView.url != url) {
                    webView.loadUrl(url)
                }
            }
        )

        // Show loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Show error message
        if (hasError) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.height(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = errorMessage,
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

    // Clean up WebView when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            // No cleanup needed here as AndroidView handles WebView lifecycle
        }
    }
}
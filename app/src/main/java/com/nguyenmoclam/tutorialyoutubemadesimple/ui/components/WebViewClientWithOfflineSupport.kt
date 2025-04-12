package com.nguyenmoclam.tutorialyoutubemadesimple.ui.components

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.OfflineDataManager
import java.io.ByteArrayInputStream

/**
 * Custom WebViewClient with offline support.
 * This class intercepts network requests and provides content from local storage when in offline mode.
 */
class WebViewClientWithOfflineSupport(
    private val offlineDataManager: OfflineDataManager,
    private val networkUtils: NetworkUtils,
    private val onPageStarted: (String?) -> Unit = {},
    private val onPageFinished: (String?) -> Unit = {},
    private val onReceivedError: (Int, String?) -> Unit = { _, _ -> }
) : WebViewClient() {

    companion object {
        private const val TAG = "WebViewClientOffline"
        private const val MIME_TYPE_HTML = "text/html"
        private const val MIME_TYPE_CSS = "text/css"
        private const val MIME_TYPE_JS = "application/javascript"
        private const val MIME_TYPE_PNG = "image/png"
        private const val MIME_TYPE_JPEG = "image/jpeg"
        private const val MIME_TYPE_SVG = "image/svg+xml"
        private const val ENCODING = "UTF-8"
    }

    // coroutineScope property has been removed as it is not used

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageStarted(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageFinished(url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        // Check network state directly instead of using isOfflineMode parameter
        val isOfflineMode = !networkUtils.isNetworkAvailable()

        if (!isOfflineMode || request == null) {
            return super.shouldInterceptRequest(view, request)
        }

        val url = request.url.toString()
        Log.d(TAG, "Processing offline request for URL: $url")

        try {
            // Determine MIME type based on URL
            val mimeType = getMimeType(url)

            // Return content from local storage
            return when {
                // Handle HTML resources
                url.endsWith(".html") || url.endsWith(".htm") || !url.contains(".") -> {
                    // Use blocking call directly as shouldInterceptRequest runs off the main thread
                    val content = offlineDataManager.getWebContentBlocking(url)
                        ?: "<html><body><h1>Content not available offline</h1></body></html>"

                    Log.d(TAG, "Loaded offline HTML content for URL: $url")
                    WebResourceResponse(
                        MIME_TYPE_HTML,
                        ENCODING,
                        ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
                    )
                }

                // Handle CSS resources
                url.endsWith(".css") -> {
                    // Use blocking call directly
                    val byteArray = offlineDataManager.getWebResourceBlocking(url)

                    if (byteArray != null) {
                        Log.d(TAG, "Loaded offline CSS for URL: $url")
                        WebResourceResponse(
                            MIME_TYPE_CSS,
                            ENCODING,
                            ByteArrayInputStream(byteArray)
                        )
                    } else {
                        // Return empty CSS if not found
                        Log.d(TAG, "CSS not found offline for URL: $url")
                        WebResourceResponse(
                            MIME_TYPE_CSS,
                            ENCODING,
                            ByteArrayInputStream("/* CSS not available offline */".toByteArray())
                        )
                    }
                }

                // Handle JavaScript resources
                url.endsWith(".js") -> {
                     // Use blocking call directly
                    val byteArray = offlineDataManager.getWebResourceBlocking(url)

                    if (byteArray != null) {
                        Log.d(TAG, "Loaded offline JavaScript for URL: $url")
                        WebResourceResponse(
                            MIME_TYPE_JS,
                            ENCODING,
                            ByteArrayInputStream(byteArray)
                        )
                    } else {
                        // Return empty JS if not found
                        Log.d(TAG, "JavaScript not found offline for URL: $url")
                        WebResourceResponse(
                            MIME_TYPE_JS,
                            ENCODING,
                            ByteArrayInputStream("// JavaScript not available offline".toByteArray())
                        )
                    }
                }

                // Handle image resources
                url.contains(".png") || url.contains(".jpg") || url.contains(".jpeg") || url.contains(
                    ".svg"
                ) -> {
                     // Use blocking call directly
                    val byteArray = offlineDataManager.getWebResourceBlocking(url)

                    if (byteArray != null) {
                        Log.d(TAG, "Loaded offline image for URL: $url")
                        WebResourceResponse(
                            mimeType,
                            ENCODING,
                            ByteArrayInputStream(byteArray)
                        )
                    } else {
                        // Return empty image if not found
                        Log.d(TAG, "Image not found offline for URL: $url")
                        WebResourceResponse(
                            mimeType,
                            ENCODING,
                            ByteArrayInputStream(ByteArray(0))
                        )
                    }
                }

                // Handle other resources (Treat as HTML by default if extension unknown)
                else -> {
                     // Use blocking call directly
                    val content = offlineDataManager.getWebContentBlocking(url) ?: ""

                    Log.d(TAG, "Loaded other offline content for URL: $url")
                    WebResourceResponse(
                        mimeType,
                        ENCODING,
                        ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing offline request: ${e.message}")
            // Call onReceivedError callback to notify error
            view?.post {
                onReceivedError(-1, "Error loading offline content: ${e.message}")
            }
            // Return an empty WebResourceResponse instead of calling super to avoid network requests
            return WebResourceResponse(
                "text/plain",
                ENCODING,
                ByteArrayInputStream("Error: ${e.message}".toByteArray(Charsets.UTF_8))
            )
        }
    }

    /**
     * Determine MIME type based on URL
     */
    private fun getMimeType(url: String): String {
        return when {
            url.endsWith(".html") || url.endsWith(".htm") -> MIME_TYPE_HTML
            url.endsWith(".css") -> MIME_TYPE_CSS
            url.endsWith(".js") -> MIME_TYPE_JS
            url.endsWith(".png") -> MIME_TYPE_PNG
            url.endsWith(".jpg") || url.endsWith(".jpeg") -> MIME_TYPE_JPEG
            url.endsWith(".svg") -> MIME_TYPE_SVG
            else -> MIME_TYPE_HTML
        }
    }
}

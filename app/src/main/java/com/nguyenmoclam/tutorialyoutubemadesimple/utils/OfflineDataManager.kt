package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import android.content.Context
import android.util.Log
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.MindMap
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Summary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages offline data for the application.
 * This class is responsible for storing and retrieving HTML content, SVGs, and other data
 * so that users can access them without an internet connection.
 */
@Singleton
class OfflineDataManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "OfflineDataManager"
        private const val OFFLINE_DIR = "offline_data"
        private const val SUMMARY_DIR = "summaries"
        private const val MINDMAP_DIR = "mindmaps"
        private const val WEBVIEW_DIR = "webview"
        private const val IMAGES_DIR = "images"
        private const val HTML_EXTENSION = ".html"
        private const val SVG_EXTENSION = ".svg"
        private const val CSS_EXTENSION = ".css"
        private const val JS_EXTENSION = ".js"

        // Default is 500MB
        private const val DEFAULT_MAX_CACHE_SIZE = 500L * 1024L * 1024L

        // Threshold to start cache cleanup (80% of max capacity)
        private const val CACHE_CLEANUP_THRESHOLD_PERCENT = 0.8

        // Current cache size, updated from OfflineSettingsManager
        private var currentMaxCacheSize = DEFAULT_MAX_CACHE_SIZE
    }

    // LRU cache for data with default size
    private var cache: LRUCache<String, ByteArray> = LRUCache(currentMaxCacheSize)

    /**
     * Save HTML content of a summary for a specific quiz
     */
    suspend fun saveSummaryHtml(quizId: Long, htmlContent: String) = withContext(Dispatchers.IO) {
        try {
            val file = getSummaryFile(quizId)
            file.parentFile?.mkdirs()
            val content = htmlContent.toByteArray()
            FileOutputStream(file).use { it.write(content) }

            // Add to cache
            val cacheKey = "summary_${quizId}"
            cache.put(cacheKey, content, content.size.toLong())

            // Check and cleanup cache if needed
            checkAndCleanupCache()

            Log.d(TAG, "Saved HTML summary for quiz $quizId")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving HTML summary: ${e.message}")
        }
    }

    /**
     * Get HTML content of a summary for a specific quiz
     */
    suspend fun getSummaryHtml(quizId: Long): String? = withContext(Dispatchers.IO) {
        try {
            val cacheKey = "summary_${quizId}"

            // Try to get from cache first
            cache.get(cacheKey)?.let {
                return@withContext String(it)
            }

            // If not in cache, read from file
            val file = getSummaryFile(quizId)
            if (!file.exists()) return@withContext null

            FileInputStream(file).use { fis ->
                val size = fis.available()
                val buffer = ByteArray(size)
                fis.read(buffer)

                // Add to cache
                cache.put(cacheKey, buffer, buffer.size.toLong())

                return@withContext String(buffer)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading HTML summary: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Save SVG content of a mind map for a specific quiz
     */
    suspend fun saveMindMapSvg(quizId: Long, svgContent: String) = withContext(Dispatchers.IO) {
        try {
            val file = getMindMapFile(quizId)
            file.parentFile?.mkdirs()
            val content = svgContent.toByteArray()
            FileOutputStream(file).use { it.write(content) }

            // Add to cache
            val cacheKey = "mindmap_${quizId}"
            cache.put(cacheKey, content, content.size.toLong())

            // Check and cleanup cache if needed
            checkAndCleanupCache()

            Log.d(TAG, "Saved SVG mind map for quiz $quizId")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving SVG mind map: ${e.message}")
        }
    }

    /**
     * Get SVG content of a mind map for a specific quiz
     */
    suspend fun getMindMapSvg(quizId: Long): String? = withContext(Dispatchers.IO) {
        try {
            val cacheKey = "mindmap_${quizId}"

            // Try to get from cache first
            cache.get(cacheKey)?.let {
                return@withContext String(it)
            }

            // If not in cache, read from file
            val file = getMindMapFile(quizId)
            if (!file.exists()) return@withContext null

            FileInputStream(file).use { fis ->
                val size = fis.available()
                val buffer = ByteArray(size)
                fis.read(buffer)

                // Add to cache
                cache.put(cacheKey, buffer, buffer.size.toLong())

                return@withContext String(buffer)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading SVG mind map: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Check if summary is available offline for a specific quiz
     */
    fun isSummaryAvailableOffline(quizId: Long): Boolean {
        return getSummaryFile(quizId).exists()
    }

    /**
     * Check if mind map is available offline for a specific quiz
     */
    fun isMindMapAvailableOffline(quizId: Long): Boolean {
        return getMindMapFile(quizId).exists()
    }

    /**
     * Clear all offline data
     */
    @Suppress("unused") // May be used in the future
    suspend fun clearAllOfflineData() = withContext(Dispatchers.IO) {
        try {
            val offlineDir = File(context.filesDir, OFFLINE_DIR)
            if (offlineDir.exists()) {
                offlineDir.deleteRecursively()
            }

            // Clear cache
            cache.clear()

            Log.d(TAG, "Cleared all offline data and cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing offline data: ${e.message}")
        }
    }

    /**
     * Clear offline data for a specific quiz
     */
    suspend fun clearQuizOfflineData(quizId: Long) = withContext(Dispatchers.IO) {
        try {
            val summaryFile = getSummaryFile(quizId)
            if (summaryFile.exists()) {
                summaryFile.delete()
            }

            val mindMapFile = getMindMapFile(quizId)
            if (mindMapFile.exists()) {
                mindMapFile.delete()
            }

            // Remove from cache
            cache.remove("summary_${quizId}")
            cache.remove("mindmap_${quizId}")

            Log.d(TAG, "Cleared offline data and cache for quiz $quizId")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing offline data for quiz: ${e.message}")
        }
    }

    /**
     * Synchronize offline data with online data
     */
    suspend fun syncOfflineData(quiz: Quiz, summary: Summary?, mindMap: MindMap?) =
        withContext(Dispatchers.IO) {
            try {
                // Save summary if available
                if (summary != null && summary.content.isNotBlank()) {
                    saveSummaryHtml(quiz.id, summary.content)
                }

                // Save mind map if available
                if (mindMap != null && mindMap.mermaidCode.isNotBlank()) {
                    saveMindMapSvg(quiz.id, mindMap.mermaidCode)
                }

                Log.d(TAG, "Synchronized offline data for quiz ${quiz.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing offline data: ${e.message}")
            }
        }

    /**
     * Get size of offline data (in bytes)
     */
    suspend fun getOfflineDataSize(): Long = withContext(Dispatchers.IO) {
        try {
            val offlineDir = File(context.filesDir, OFFLINE_DIR)
            val fileSize = if (offlineDir.exists()) calculateDirSize(offlineDir) else 0L
            val cacheSize = cache.getCurrentSize()

            return@withContext fileSize + cacheSize
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating offline data size: ${e.message}")
            return@withContext 0L
        }
    }

    /**
     * Manage offline storage
     */
    suspend fun manageOfflineStorage() = withContext(Dispatchers.IO) {
        try {
            val maxSize = DEFAULT_MAX_CACHE_SIZE
            val threshold = (maxSize * CACHE_CLEANUP_THRESHOLD_PERCENT).toLong()
            val totalSize = getOfflineDataSize()

            if (totalSize > threshold) {
                Log.d(
                    TAG,
                    "Cache size ($totalSize bytes) exceeds threshold ($threshold bytes), starting cleanup"
                )

                // Delete oldest items in cache until size is below threshold
                var currentCacheSize = cache.getCurrentSize()
                val keysToRemove = mutableListOf<String>()

                // Get list of keys to remove
                for (key in cache.getKeys()) {
                    if (currentCacheSize <= threshold) break

                    cache.get(key)?.let { value ->
                        currentCacheSize -= value.size.toLong()
                        keysToRemove.add(key)
                    }
                }

                // Remove selected keys
                for (key in keysToRemove) {
                    cache.remove(key)
                    Log.d(TAG, "Removed cache item: $key")
                }

                // If still over threshold, delete least used files
                if (getOfflineDataSize() > threshold) {
                    cleanupOldestFiles(threshold)
                }

                Log.d(TAG, "Completed cache cleanup, current size: ${getOfflineDataSize()} bytes")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error managing storage: ${e.message}")
        }
    }

    /**
     * Check and cleanup cache if needed
     * Uses LRU (Least Recently Used) to remove least used items
     */
    private suspend fun checkAndCleanupCache() = withContext(Dispatchers.IO) {
        try {
            val totalSize = getOfflineDataSize()
            val threshold = (DEFAULT_MAX_CACHE_SIZE * CACHE_CLEANUP_THRESHOLD_PERCENT).toLong()

            if (totalSize > threshold) {
                Log.d(
                    TAG,
                    "Cache size ($totalSize bytes) exceeds threshold ($threshold bytes), starting cleanup"
                )

                // Delete oldest items in cache until size is below threshold
                var currentCacheSize = cache.getCurrentSize()
                val keysToRemove = mutableListOf<String>()

                // Get list of keys to remove
                for (key in cache.getKeys()) {
                    if (currentCacheSize <= threshold) break

                    cache.get(key)?.let { value ->
                        currentCacheSize -= value.size.toLong()
                        keysToRemove.add(key)
                    }
                }

                // Remove selected keys
                for (key in keysToRemove) {
                    cache.remove(key)
                    Log.d(TAG, "Removed cache item: $key")
                }

                // If still over threshold, delete least used files
                if (getOfflineDataSize() > threshold) {
                    cleanupOldestFiles(threshold)
                }

                Log.d(TAG, "Completed cache cleanup, current size: ${getOfflineDataSize()} bytes")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up cache: ${e.message}")
        }
    }

    /**
     * Clean up oldest files to reduce size below threshold
     */
    private suspend fun cleanupOldestFiles(threshold: Long) = withContext(Dispatchers.IO) {
        try {
            val offlineDir = File(context.filesDir, OFFLINE_DIR)
            if (!offlineDir.exists()) return@withContext

            // Get list of all files and sort by access time
            val allFiles = mutableListOf<File>()
            collectFiles(offlineDir, allFiles)

            if (allFiles.isEmpty()) {
                Log.d(TAG, "No files to clean up")
                return@withContext
            }

            // Sort by modification time (oldest first)
            allFiles.sortBy { it.lastModified() }

            // Delete oldest files until size is below threshold
            var currentSize = getOfflineDataSize()
            Log.d(
                TAG,
                "Starting file cleanup. Current size: $currentSize bytes, threshold: $threshold bytes"
            )

            var filesDeleted = 0
            for (file in allFiles) {
                if (currentSize <= threshold) break

                val fileSize = file.length()
                if (file.delete()) {
                    filesDeleted++
                    currentSize -= fileSize
                    Log.d(TAG, "Deleted file: ${file.name}, size: $fileSize bytes")
                }

                // Pause after every 5 files to avoid overload
                if (filesDeleted % 5 == 0) {
                    delay(50)
                }
            }

            Log.d(TAG, "Deleted $filesDeleted files, size after cleanup: $currentSize bytes")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old files: ${e.message}")
        }
    }

    /**
     * Collect all files in a directory and its subdirectories
     */
    private fun collectFiles(directory: File, files: MutableList<File>) {
        val fileList = directory.listFiles() ?: return
        for (file in fileList) {
            if (file.isDirectory) {
                collectFiles(file, files)
            } else {
                files.add(file)
            }
        }
    }

    /**
     * Calculate size of a directory in bytes
     */
    private fun calculateDirSize(dir: File): Long {
        var size = 0L
        val files = dir.listFiles()
        if (files != null) {
            for (file in files) {
                size += if (file.isDirectory) {
                    calculateDirSize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }

    /**
     * Get summary file for a specific quiz
     */
    private fun getSummaryFile(quizId: Long): File {
        val dir = File(context.filesDir, "$OFFLINE_DIR/$SUMMARY_DIR")
        return File(dir, "$quizId$HTML_EXTENSION")
    }

    /**
     * Get mind map file for a specific quiz
     */
    private fun getMindMapFile(quizId: Long): File {
        val dir = File(context.filesDir, "$OFFLINE_DIR/$MINDMAP_DIR")
        return File(dir, "$quizId$SVG_EXTENSION")
    }

    /**
     * Save web content for offline use
     * @param url URL of web content
     * @param content Web content to save
     * @param processEmbeddedResources Whether to process embedded resources, defaults to true
     * @param processedUrls List of processed URLs to avoid infinite recursion
     * @return Unit
     */
    suspend fun saveWebContent(
        url: String,
        content: String,
        processEmbeddedResources: Boolean = true,
        processedUrls: MutableSet<String> = mutableSetOf()
    ): Unit = withContext(Dispatchers.IO) {
        try {
            // Check if URL has been processed to avoid recursion
            if (url in processedUrls) {
                Log.d(TAG, "URL already processed: $url")
                return@withContext
            }

            // Add current URL to processed list
            processedUrls.add(url)

            val file = getWebContentFile(url)
            file.parentFile?.mkdirs()
            FileOutputStream(file).use { it.write(content.toByteArray()) }
            Log.d(TAG, "Saved web content for URL: $url")

            // Parse and save embedded resources (CSS, JS, images) if requested
            if (processEmbeddedResources) {
                extractAndSaveEmbeddedResources(content, url, processedUrls)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving web content: ${e.message}")
        }
    }

    /**
     * Get saved web content (suspend version).
     */
    suspend fun getWebContent(url: String): String? = withContext(Dispatchers.IO) {
        getWebContentBlocking(url) // Delegate to blocking version within IO context
    }

    /**
     * Get saved web content (BLOCKING version for synchronous contexts like WebViewClient).
     * Performs file IO directly on the calling thread. DO NOT CALL ON MAIN THREAD.
     */
    fun getWebContentBlocking(url: String): String? {
        try {
            val file = getWebContentFile(url)
            if (!file.exists()) return null

            FileInputStream(file).use { fis ->
                val size = fis.available()
                val buffer = ByteArray(size)
                fis.read(buffer)
                return String(buffer)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading web content: ${e.message}")
            return null
        }
    }

    /**
     * Check if web content is available offline
     */
    fun isWebContentAvailableOffline(url: String): Boolean {
        return getWebContentFile(url).exists()
    }

    /**
     * Save web resource (CSS, JS, images) for offline use
     */
    suspend fun saveWebResource(url: String, content: ByteArray) = withContext(Dispatchers.IO) {
        try {
            val file = getWebResourceFile(url)
            file.parentFile?.mkdirs()
            FileOutputStream(file).use { it.write(content) }
            Log.d(TAG, "Saved web resource for URL: $url")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving web resource: ${e.message}")
        }
    }

     /**
     * Get saved web resource (suspend version).
     */
    suspend fun getWebResource(url: String): ByteArray? = withContext(Dispatchers.IO) {
        getWebResourceBlocking(url) // Delegate to blocking version within IO context
    }

    /**
     * Get saved web resource (BLOCKING version for synchronous contexts like WebViewClient).
     * Performs file IO directly on the calling thread. DO NOT CALL ON MAIN THREAD.
     */
    fun getWebResourceBlocking(url: String): ByteArray? {
        try {
            val file = getWebResourceFile(url)
            if (!file.exists()) return null // Use standard return

            FileInputStream(file).use { fis ->
                // Reading size via available() might be unreliable for large files,
                // but often okay for typical web resources. A more robust way
                // would be to read in chunks, but let's keep it simple for now.
                val size = fis.available()
                if (size <= 0) return ByteArray(0) // Handle empty file case
                val buffer = ByteArray(size)
                val bytesRead = fis.read(buffer)
                if (bytesRead < size) {
                    // Handle case where not all bytes were read (unlikely with available())
                    Log.w(TAG, "Could not read entire web resource file: $url")
                    return buffer.copyOf(bytesRead) // Return what was read
                }
                return buffer // Use standard return
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading web resource: ${e.message}")
            return null
        }
    }

    /**
     * Check if web resource is available offline
     */
    fun isWebResourceAvailableOffline(url: String): Boolean {
        return getWebResourceFile(url).exists()
    }

    /**
     * Get file for web content
     */
    private fun getWebContentFile(url: String): File {
        val dir = File(context.filesDir, "$OFFLINE_DIR/$WEBVIEW_DIR")
        val fileName = url.hashCode().toString()
        return File(dir, "$fileName$HTML_EXTENSION")
    }

    /**
     * Get file for web resource
     */
    fun getWebResourceFile(url: String): File { // Made public
        val dir = File(context.filesDir, "$OFFLINE_DIR/$WEBVIEW_DIR")
        val fileName = url.hashCode().toString()
        val extension = when {
            url.endsWith(".css") -> CSS_EXTENSION
            url.endsWith(".js") -> JS_EXTENSION
            else -> ""
        }
        return File(dir, "$fileName$extension")
    }

    /**
     * Parse and save embedded resources (CSS, JS, images)
     * @param htmlContent HTML content to parse
     * @param baseUrl Base URL to build URL for resources
     * @param processedUrls List of processed URLs to avoid infinite recursion
     * @return Unit
     */
    private suspend fun extractAndSaveEmbeddedResources(
        htmlContent: String,
        baseUrl: String,
        processedUrls: MutableSet<String>
    ): Unit = withContext(Dispatchers.IO) {
        try {
            // Find CSS links
            val cssPattern: Regex = "<link[^>]+href=\"([^\"]+\\.css)\"[^>]*>".toRegex()
            cssPattern.findAll(htmlContent).forEach { matchResult ->
                val cssUrl: String = matchResult.groupValues[1]
                val fullUrl = resolveUrl(cssUrl, baseUrl)
                if (!processedUrls.contains(fullUrl)) {
                    downloadAndSaveResource(cssUrl, baseUrl, processedUrls)
                }
            }

            // Find JS script
            val jsPattern: Regex = "<script[^>]+src=\"([^\"]+\\.js)\"[^>]*>".toRegex()
            jsPattern.findAll(htmlContent).forEach { matchResult ->
                val jsUrl: String = matchResult.groupValues[1]
                val fullUrl = resolveUrl(jsUrl, baseUrl)
                if (!processedUrls.contains(fullUrl)) {
                    downloadAndSaveResource(jsUrl, baseUrl, processedUrls)
                }
            }

            val imgPattern: Regex = "<img[^>]+src=\"([^\"]+)\"[^>]*>".toRegex()
            imgPattern.findAll(htmlContent).forEach { matchResult ->
                val imgUrl: String = matchResult.groupValues[1]
                val fullUrl = resolveUrl(imgUrl, baseUrl)
                if (!processedUrls.contains(fullUrl)) {
                    downloadAndSaveResource(imgUrl, baseUrl, processedUrls)
                }
            }

            // Find SVG links
            val svgPattern: Regex = "<svg[^>]*>.*?</svg>".toRegex(RegexOption.DOT_MATCHES_ALL)
            svgPattern.findAll(htmlContent).forEach { matchResult ->
                val svgContent: String = matchResult.value
                // Save SVG embedded as a separate resource, do not process embedded resources in SVG
                val svgUrl = "${baseUrl}_embedded_svg_${matchResult.range.first}"
                if (!processedUrls.contains(svgUrl)) {
                    // Pass processedUrls to avoid infinite recursion
                    saveWebContent(
                        svgUrl,
                        svgContent,
                        processEmbeddedResources = false,
                        processedUrls = processedUrls
                    )
                }
            }

            Log.d(TAG, "Analyzed and saved embedded resources from $baseUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing embedded resources: ${e.message}")
        }
    }

    /**
     * Build full URL from relative URL and base URL
     */
    private fun resolveUrl(url: String, baseUrl: String): String {
        return if (url.startsWith("http")) {
            url
        } else if (url.startsWith("/")) {
            // URL relative with base URL
            try {
                val baseUri = java.net.URI(baseUrl)
                "${baseUri.scheme}://${baseUri.host}${url}"
            } catch (e: Exception) {
                "$baseUrl$url"
            }
        } else {
            // URL relative with current page
            val baseDir = baseUrl.substringBeforeLast("/")
            "$baseDir/$url"
        }
    }

    /**
     * Parses and saves embedded resources in CSS.
     * @param cssContent The CSS content to be parsed
     * @param baseUrl The base URL used to construct full URLs for the resources
     * @param processedUrls A list of already processed URLs to prevent infinite recursion
     * @return Unit
     */
    private suspend fun extractResourcesFromCss(
        cssContent: String,
        baseUrl: String,
        processedUrls: MutableSet<String>
    ): Unit = withContext(Dispatchers.IO) {
        try {
            // Find URL in CSS (background-image, @import, font-face, etc.)
            val urlPattern = "url\\(['\"]?([^'\")]+)['\"]?\\)".toRegex()
            val importPattern = "@import\\s+['\"]([^'\"]+)['\"];".toRegex()

            // Process URLs in url()
            urlPattern.findAll(cssContent).forEach { matchResult ->
                val resourceUrl = matchResult.groupValues[1]
                if (!resourceUrl.startsWith("data:")) { // Skip data URLs
                    val fullUrl = resolveUrl(resourceUrl, baseUrl)
                    if (fullUrl !in processedUrls) {
                        downloadAndSaveResource(resourceUrl, baseUrl, processedUrls)
                    }
                }
            }

            // Process URLs in @import
            importPattern.findAll(cssContent).forEach { matchResult ->
                val importUrl = matchResult.groupValues[1]
                val fullUrl = resolveUrl(importUrl, baseUrl)
                if (fullUrl !in processedUrls) {
                    downloadAndSaveResource(importUrl, baseUrl, processedUrls)
                }
            }

            Log.d(TAG, "Analyzed and saved embedded resources from $baseUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error when analyzing resources from CSS: ${e.message}")
        }
    }

    /**
     * Downloads and saves a resource from the given URL.
     * @param resourceUrl The URL of the resource to be downloaded
     * @param baseUrl The base URL used to construct the full URL
     * @param processedUrls A list of already processed URLs to prevent infinite recursion
     * @return Unit
     */
    private suspend fun downloadAndSaveResource(
        resourceUrl: String,
        baseUrl: String,
        processedUrls: MutableSet<String>
    ): Unit = withContext(Dispatchers.IO) {
        try {
            // Build full URL if it's a relative URL
            val fullUrl = resolveUrl(resourceUrl, baseUrl)

            // Check if the URL has been processed
            if (fullUrl in processedUrls) {
                Log.d(TAG, "URL processed already: $fullUrl")
                return@withContext
            }

            // Add URL to processed list to prevent infinite recursion
            processedUrls.add(fullUrl)

            // Check if the resource has been saved
            if (isWebResourceAvailableOffline(fullUrl) || isWebContentAvailableOffline(fullUrl)) {
                Log.d(TAG, "Resource already saved: $fullUrl")
                return@withContext
            }

            // Download resource
            try {
                val connection: java.net.URLConnection = URL(fullUrl).openConnection()
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val contentType: String = connection.contentType ?: ""

                if (contentType.contains("text/") || fullUrl.endsWith(".css") || fullUrl.endsWith(".js")) {
                    // Save content as text
                    val content: String =
                        connection.getInputStream().bufferedReader().use { it.readText() }
                    // Pass processedUrls to avoid infinite recursion
                    saveWebContent(
                        fullUrl,
                        content,
                        processEmbeddedResources = false,
                        processedUrls = processedUrls
                    )

                    // If it is CSS, it may require further analysis to find embedded resources (like images, fonts)
                    if (fullUrl.endsWith(".css")) {
                        val cssProcessKey = "${fullUrl}_processed"
                        if (!processedUrls.contains(cssProcessKey)) {
                            processedUrls.add(cssProcessKey)
                            extractResourcesFromCss(content, fullUrl, processedUrls)
                        }
                    }
                } else {
                    // Save content as binary
                    val bytes: ByteArray = connection.getInputStream().use { it.readBytes() }
                    saveWebResource(fullUrl, bytes)
                }

                Log.d(TAG, "Saved and downloaded resource: $fullUrl")
            } catch (e: Exception) {
                Log.e(TAG, "Cannot download resource: $fullUrl, error: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error when processing resource: $resourceUrl, error: ${e.message}")
        }
    }
}

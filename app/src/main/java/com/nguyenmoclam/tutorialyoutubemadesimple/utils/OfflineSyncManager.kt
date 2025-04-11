package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import android.util.Log
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages data synchronization between online and offline states.
 * This class monitors network status and automatically synchronizes data when network connection is restored.
 */
@Singleton
class OfflineSyncManager @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val quizRepository: QuizRepository,
    private val offlineDataManager: OfflineDataManager
) {
    companion object {
        private const val TAG = "OfflineSyncManager"
        private const val TIMEOUT_MILLIS = 15000 // 15 seconds
        private const val BUFFER_SIZE = 8192 // 8KB

        // Configuration for retry mechanism
        private const val MAX_RETRIES = 5 // Increased retry count for better reliability
        private const val INITIAL_BACKOFF_DELAY = 500L // 500ms - Reduced initial wait time
        private const val MAX_BACKOFF_DELAY = 15000L // 15 seconds - Reduced maximum wait time
        private const val BACKOFF_MULTIPLIER =
            1.5 // Reduced multiplier for more gradual wait time increase

        // Configuration for circuit breaker
        private const val FAILURE_THRESHOLD =
            5 // Number of consecutive failures before opening circuit
        private const val RESET_TIMEOUT = 60000L // 60 seconds before retry
    }

    // Circuit breaker state
    private var failureCount = 0
    private var lastFailureTime = 0L
    private var isCircuitOpen = false

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isObservingNetworkChanges = false

    /**
     * Start monitoring network state changes and synchronize data when necessary
     */
    fun startObservingNetworkChanges() {
        if (isObservingNetworkChanges) return
        isObservingNetworkChanges = true

        coroutineScope.launch {
            networkUtils.observeNetworkConnectivity()
                .distinctUntilChanged()
                .collectLatest { isConnected ->
                    if (isConnected) {
                        Log.d(TAG, "Network connection restored, starting data synchronization")
                        // Always automatically sync when network connection is restored
                        startSync()
                    } else {
                        Log.d(TAG, "Network connection lost, using cached data")
                        // Cached data will be used automatically when there is no network connection
                    }
                }
        }
    }

    /**
     * Start the data synchronization process
     * This method can be called externally to initiate synchronization
     * Improved to handle synchronization more smoothly and provide progress updates
     */
    fun startSync() {
        // Check for network connection before starting sync
        if (!networkUtils.isNetworkAvailable()) {
            Log.d(TAG, "No network connection, skipping synchronization")
            return
        }

        // No need to check offline mode anymore as the app automatically handles network state

        // Check circuit breaker before starting sync
        if (isCircuitOpen) {
            val timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime
            if (timeSinceLastFailure < RESET_TIMEOUT) {
                Log.w(
                    TAG,
                    "Circuit breaker is open, skipping sync request. Retry in ${(RESET_TIMEOUT - timeSinceLastFailure) / 1000} seconds"
                )
                return
            }
            // Reset circuit breaker after timeout
            isCircuitOpen = false
            failureCount = 0
            Log.d(TAG, "Circuit breaker has been reset after timeout")
        }

        // Use SupervisorJob to ensure errors in one coroutine don't affect others
        coroutineScope.launch(Dispatchers.IO + SupervisorJob()) {
            try {
                Log.d(TAG, "Starting background data synchronization")
                syncAllQuizData()
                // Storage management is now handled by OfflineDataManager's checkAndCleanupCache
            } catch (e: Exception) {
                Log.e(TAG, "Error synchronizing data: ${e.message}")
                // Increment failure count and check circuit breaker
                failureCount++
                if (failureCount >= FAILURE_THRESHOLD) {
                    isCircuitOpen = true
                    lastFailureTime = System.currentTimeMillis()
                    Log.w(TAG, "Circuit breaker opened due to too many consecutive sync failures")
                }
            }
        }
    }

    /**
     * Synchronize data for all quizzes
     * Uses incremental sync to only synchronize changed data
     */
    private suspend fun syncAllQuizData() {
        try {
            // Check circuit breaker
            if (isCircuitOpen) {
                val timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime
                if (timeSinceLastFailure < RESET_TIMEOUT) {
                    Log.w(TAG, "Circuit breaker is open, skipping sync request for all quizzes")
                    return
                }
                // Reset circuit breaker after timeout
                isCircuitOpen = false
                failureCount = 0
            }

            // Get all quizzes from database
            quizRepository.getAllQuizzes().collectLatest { quizzes ->
                // Filter quizzes that need sync based on lastSyncTimestamp (incremental sync)
                val quizzesToSync = quizzes.filter { quiz ->
                    val summary = quizRepository.getSummaryByQuizId(quiz.id)
                    val mindMap = quizRepository.getMindMapByQuizId(quiz.id)

                    // Check if sync is needed based on timestamp
                    val needSyncSummary =
                        summary?.lastSyncTimestamp?.let { it < quiz.lastUpdated } != false
                    val needSyncMindMap =
                        mindMap?.lastUpdated?.let { it < quiz.lastUpdated } != false

                    // Also check if localThumbnailPath is missing, indicating thumbnail needs sync
                    val needSyncThumbnail = quiz.localThumbnailPath.isNullOrBlank() && quiz.thumbnailUrl.isNotEmpty()

                    needSyncSummary || needSyncMindMap || needSyncThumbnail // Include thumbnail check
                }

                if (quizzesToSync.isEmpty()) {
                    Log.d(TAG, "No quizzes need synchronization")
                    return@collectLatest
                }

                Log.d(TAG, "Need to sync ${quizzesToSync.size}/${quizzes.size} quizzes")

                // Sort quizzes by update time (newest first)
                val sortedQuizzes = quizzesToSync.sortedByDescending { it.lastUpdated }

                // Sync in batches to avoid overload
                val batchSize = 3 // Reduced batch size to avoid overload
                sortedQuizzes.chunked(batchSize).forEach { batch ->
                    batch.forEach { quiz ->
                        try {
                            syncQuizData(quiz) // Sync individual quiz data (including thumbnail now)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error syncing quiz ${quiz.id}: ${e.message}")
                        }
                    }
                    // Pause between batches to avoid overload
                    delay(2000) // Increased delay between batches
                }

                // Storage management is now handled by OfflineDataManager's checkAndCleanupCache
                // Call manageOfflineStorage after syncing all quizzes
                offlineDataManager.manageOfflineStorage()

                Log.d(
                    TAG,
                    "Completed data synchronization and storage check for ${quizzesToSync.size}/${quizzes.size} quizzes"
                )
            }
        } catch (e: Exception) {
            failureCount++
            Log.e(TAG, "Error synchronizing data: ${e.message}")

            // Check circuit breaker
            if (failureCount >= FAILURE_THRESHOLD) {
                isCircuitOpen = true
                lastFailureTime = System.currentTimeMillis()
                Log.w(
                    TAG,
                    "Circuit breaker opened due to too many consecutive failures when syncing all quizzes"
                )
            }
        }
    }

    /**
     * Synchronize data for a specific quiz
     * Uses incremental sync to only synchronize changed data
     * Applies exponential backoff and circuit breaker pattern for error handling
     */
    suspend fun syncQuizData(quiz: Quiz) = withContext(Dispatchers.IO) {
        // Check for network connection
        if (!networkUtils.isNetworkAvailable()) {
            Log.d(TAG, "No network connection, skipping sync for quiz ${quiz.id}")
            return@withContext
        }

        // Check connection type restriction
        val connectionType = networkUtils.getConnectionTypeRestriction() // Use networkUtils
        if (connectionType == "wifi_only" && !networkUtils.isWifiConnection()) {
            Log.d(
                TAG,
                "Sync only allowed over WiFi, skipping sync over mobile data for quiz ${quiz.id}"
            )
            return@withContext
        }

        // Check if sync is needed based on timestamp (incremental sync)
        val summary = quizRepository.getSummaryByQuizId(quiz.id)
        val mindMap = quizRepository.getMindMapByQuizId(quiz.id)

        // Determine components that need sync
        val needSyncSummary = summary?.lastSyncTimestamp?.let { it < quiz.lastUpdated } != false
        val needSyncMindMap = mindMap?.lastUpdated?.let { it < quiz.lastUpdated } != false
        val needSyncThumbnail = quiz.localThumbnailPath.isNullOrBlank() && quiz.thumbnailUrl.isNotEmpty()


        if (!needSyncSummary && !needSyncMindMap && !needSyncThumbnail) { // Include thumbnail check
            Log.d(TAG, "Skipping sync for quiz ${quiz.id} as data is up to date")
            return@withContext
        }

        // Check circuit breaker
        if (isCircuitOpen) {
            val timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime
            if (timeSinceLastFailure < RESET_TIMEOUT) {
                Log.w(
                    TAG,
                    "Circuit breaker is open, skipping sync request for quiz ${quiz.id}. Retry in ${(RESET_TIMEOUT - timeSinceLastFailure) / 1000} seconds"
                )
                return@withContext
            }
            // Reset circuit breaker after timeout
            isCircuitOpen = false
            failureCount = 0
            Log.d(TAG, "Circuit breaker has been reset after timeout")
        }

        // Use exponential backoff for retry
        var currentDelay = INITIAL_BACKOFF_DELAY
        var summarySyncSuccess = false
        var mindMapSyncSuccess = false
        var thumbnailSyncSuccess = false

        // Synchronize summary if needed
        if (needSyncSummary) {
            repeat(MAX_RETRIES) { attempt ->
                try {
                    val currentSummary = quizRepository.getSummaryByQuizId(quiz.id)
                    if (currentSummary != null) {
                        if (currentSummary.content.isBlank()) {
                            Log.w(TAG, "Empty summary content for quiz ${quiz.id}, skipping sync")
                            summarySyncSuccess = true // Treat as success if content is blank
                            return@repeat // Correct way to exit repeat loop
                        }
                        offlineDataManager.saveSummaryHtml(quiz.id, currentSummary.content)
                        quizRepository.updateSummaryLastSyncTimestamp(currentSummary.id)
                        downloadResourcesFromHtml(currentSummary.content) // Download embedded resources
                        Log.d(TAG, "Synchronized summary for quiz ${quiz.id}")
                        summarySyncSuccess = true
                        return@repeat // Correct way to exit repeat loop
                    } else {
                         Log.w(TAG, "Summary not found in repository for quiz ${quiz.id} during sync attempt ${attempt + 1}")
                         summarySyncSuccess = true // Consider this a success if summary is expected to be potentially null
                         return@repeat // Correct way to exit repeat loop
                    }
                } catch (e: Exception) {
                    failureCount++
                    Log.e(TAG, "Error syncing summary for quiz ${quiz.id} (attempt ${attempt + 1}): ${e.message}")
                    if (failureCount >= FAILURE_THRESHOLD) { isCircuitOpen = true; lastFailureTime = System.currentTimeMillis(); Log.w(TAG, "Circuit breaker opened (summary sync)"); return@withContext }
                    if (attempt < MAX_RETRIES - 1) { delay(currentDelay); currentDelay = (currentDelay * BACKOFF_MULTIPLIER).toLong().coerceAtMost(MAX_BACKOFF_DELAY) }
                }
            }
        } else {
            summarySyncSuccess = true // Mark as success if not needed
        }

        // Synchronize mindMap if needed
        if (needSyncMindMap) {
            currentDelay = INITIAL_BACKOFF_DELAY // Reset delay
            repeat(MAX_RETRIES) { attempt ->
                try {
                    val currentMindMap = quizRepository.getMindMapByQuizId(quiz.id)
                    if (currentMindMap != null) {
                        if (currentMindMap.mermaidCode.isBlank()) {
                            Log.w(TAG, "Empty mindMap content for quiz ${quiz.id}, skipping sync")
                            mindMapSyncSuccess = true // Treat as success
                            return@repeat // Correct way to exit repeat loop
                        }
                        offlineDataManager.saveMindMapSvg(quiz.id, currentMindMap.mermaidCode)
                        Log.d(TAG, "Synchronized mindMap for quiz ${quiz.id}")
                        mindMapSyncSuccess = true
                        return@repeat // Correct way to exit repeat loop
                    } else {
                         Log.w(TAG, "MindMap not found in repository for quiz ${quiz.id} during sync attempt ${attempt + 1}")
                         mindMapSyncSuccess = true
                         return@repeat // Correct way to exit repeat loop
                    }
                } catch (e: Exception) {
                    failureCount++
                    Log.e(TAG, "Error syncing mindMap for quiz ${quiz.id} (attempt ${attempt + 1}): ${e.message}")
                    if (failureCount >= FAILURE_THRESHOLD) { isCircuitOpen = true; lastFailureTime = System.currentTimeMillis(); Log.w(TAG, "Circuit breaker opened (mindmap sync)"); return@withContext }
                    if (attempt < MAX_RETRIES - 1) { delay(currentDelay); currentDelay = (currentDelay * BACKOFF_MULTIPLIER).toLong().coerceAtMost(MAX_BACKOFF_DELAY) }
                }
            }
        } else {
            mindMapSyncSuccess = true // Mark as success if not needed
        }

        // Synchronize thumbnail if needed
        if (needSyncThumbnail) {
            currentDelay = INITIAL_BACKOFF_DELAY // Reset delay
            repeat(MAX_RETRIES) { attempt ->
                try {
                    // Download and save the thumbnail
                    downloadAndSaveWebResource(quiz.thumbnailUrl) // This function handles its own retries internally now

                    // Get the local file path *after* successful download
                    val localFile = offlineDataManager.getWebResourceFile(quiz.thumbnailUrl)
                    if (localFile.exists()) {
                        val localPath = localFile.absolutePath
                        // Update the QuizEntity in the repository
                        quizRepository.updateQuizLocalThumbnailPath(quiz.id, localPath)
                        Log.d(TAG, "Synchronized thumbnail and updated local path for quiz ${quiz.id}")
                        thumbnailSyncSuccess = true
                        return@repeat // Correctly exit repeat loop
                    } else {
                         Log.e(TAG, "Thumbnail file not found after supposed download for quiz ${quiz.id}")
                         throw Exception("Thumbnail file not found after download")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during thumbnail sync process for quiz ${quiz.id} (attempt ${attempt + 1}): ${e.message}")
                    if (attempt >= MAX_RETRIES - 1) {
                         Log.e(TAG, "Thumbnail sync failed after $MAX_RETRIES attempts for quiz ${quiz.id}")
                    }
                    // Backoff for *this* loop if downloadAndSaveWebResource failed internally
                    if (attempt < MAX_RETRIES - 1) {
                        delay(currentDelay)
                        currentDelay = (currentDelay * BACKOFF_MULTIPLIER).toLong().coerceAtMost(MAX_BACKOFF_DELAY)
                    }
                }
            }
        } else {
            thumbnailSyncSuccess = true // Mark as success if not needed or no URL
        }

        // Reset failure count only if ALL necessary syncs succeeded
        if ( (needSyncSummary && summarySyncSuccess) || !needSyncSummary ) {
             if ( (needSyncMindMap && mindMapSyncSuccess) || !needSyncMindMap ) {
                 if ( (needSyncThumbnail && thumbnailSyncSuccess) || !needSyncThumbnail ) {
                    failureCount = 0 // Reset only if all required parts succeeded
                    Log.d(TAG, "Completed data synchronization for quiz ${quiz.id}")
                    // Consider updating an overall sync status if needed
                    // quizRepository.updateQuizSyncStatus(quiz.id, true)
                 } else {
                      Log.w(TAG, "Thumbnail sync failed for quiz ${quiz.id}")
                 }
             } else {
                 Log.w(TAG, "MindMap sync failed for quiz ${quiz.id}")
             }
        } else {
             Log.w(TAG, "Summary sync failed for quiz ${quiz.id}")
        }

        // Log if any part failed after retries
        if (!summarySyncSuccess || !mindMapSyncSuccess || !thumbnailSyncSuccess) {
             Log.w(TAG, "Could not fully sync data for quiz ${quiz.id} after $MAX_RETRIES attempts")
        }
    }


    /**
     * Parse HTML content and download related resources
     * Improved error handling and condition checking before downloading
     */
    private suspend fun downloadResourcesFromHtml(htmlContent: String) =
        withContext(Dispatchers.IO) {
            // Check for empty HTML content
            if (htmlContent.isBlank()) {
                Log.w(TAG, "Empty HTML content, skipping resource download")
                return@withContext
            }

            // Check for network connection
            if (!networkUtils.isNetworkAvailable()) {
                Log.d(TAG, "No network connection, cannot download resources from HTML")
                return@withContext
            }

            // Check connection type restriction
            val connectionType = networkUtils.getConnectionTypeRestriction() // Use networkUtils
            if (connectionType == "wifi_only" && !networkUtils.isWifiConnection()) {
                Log.d(TAG, "Download only allowed over WiFi, skipping HTML resource download")
                return@withContext
            }

            if (isCircuitOpen) {
                val timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime
                if (timeSinceLastFailure < RESET_TIMEOUT) {
                    Log.w(
                        TAG,
                        "Circuit breaker is open, skipping HTML resource download. Retry in ${(RESET_TIMEOUT - timeSinceLastFailure) / 1000} seconds"
                    )
                    return@withContext
                }
                isCircuitOpen = false
                failureCount = 0
                Log.d(TAG, "Circuit breaker has been reset after timeout")
            }

            try {
                // List of patterns to find URLs in HTML
                val patterns = listOf(
                    """src=['"]([^'"]+)['"]""" to "src",
                    """href=['"]([^'"]+)['"]""" to "href",
                    """url\(['"]?([^'")]+)['"]?\)""" to "url"
                )

                val urls = mutableSetOf<String>()

                // Find all URLs in the HTML
                for ((pattern, type) in patterns) {
                    val regex = Regex(pattern)
                    val matches = regex.findAll(htmlContent)

                    for (match in matches) {
                        val url = match.groupValues[1]
                        if (url.isNotEmpty() && !url.startsWith("data:") && !url.startsWith("#") && !url.startsWith(
                                "javascript:"
                            )
                        ) {
                            // Only process resource URLs (CSS, JS, images)
                            if (url.endsWith(".css") || url.endsWith(".js") ||
                                url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".jpeg") ||
                                url.endsWith(".gif") || url.endsWith(".svg")
                            ) {
                                urls.add(url)
                            }
                        }
                    }
                }

                // Download all resources found in the HTML
                for (url in urls) {
                    try {
                        downloadAndSaveWebResource(url) // This function handles its own retries
                    } catch (e: Exception) {
                        // Log error but continue with other resources
                        Log.e(TAG, " Error downloading resource: $url: ${e.message}")
                    }
                }

                Log.d(TAG, "Attempted download for ${urls.size} resources from HTML")
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing and downloading resources from HTML: ${e.message}")
                // Don't increment failure count here as individual downloads handle it
            }
        }

    /**
     * Sync data for a specific quiz using its ID
     */
    suspend fun syncQuizDataById(quizId: Long) {
        try {
            val quiz = quizRepository.getQuizById(quizId) ?: return
            syncQuizData(quiz)
        } catch (e: Exception) {
            Log.e(TAG, "Error when syncing data for quiz $quizId: ${e.message}")
        }
    }

    /**
     * Stop observing network changes
     */
    fun stopObservingNetworkChanges() {
        isObservingNetworkChanges = false
        // Consider cancelling ongoing coroutineScope jobs if necessary
        // coroutineScope.cancel() // Might be too aggressive?
         Log.d(TAG, "Stopped observing network changes.")
    }

    /**
     * Download and save web content for offline use
     */
    suspend fun downloadAndSaveWebContent(url: String): Unit = withContext(Dispatchers.IO) {
        if (!networkUtils.isNetworkAvailable()) {
            Log.d(TAG, "No internet connection, cannot download web content")
            return@withContext
        }

        try {
            // Download web content
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS

            val content = connection.inputStream.bufferedReader().use { it.readText() }
            // Use a new set to track processed URLs
            val processedUrls =
                mutableSetOf<String>(url) // Add the current URL to the set of processed URLs
            offlineDataManager.saveWebContent(url, content, processEmbeddedResources = true, processedUrls = processedUrls) // Process embedded resources

            Log.d(TAG, "Downloaded and saved web content for URL: $url")
        } catch (e: Exception) {
            Log.e(TAG, "Error when downloading web content: ${e.message}")
        }
    }

    /**
     * Download and save web resources (CSS, JS, images) for offline use
     */
    suspend fun downloadAndSaveWebResource(url: String) = withContext(Dispatchers.IO) {
        if (url.isBlank() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            Log.w(TAG, "URL invalid: $url")
            return@withContext
        }

        // Check if already available offline to avoid redundant downloads
        if (offlineDataManager.isWebResourceAvailableOffline(url)) {
             Log.d(TAG, "Web resource already available offline: $url")
             return@withContext
        }


        // Check for network connection
        if (!networkUtils.isNetworkAvailable()) {
            Log.d(TAG, "No network connection, cannot download web resource")
            return@withContext
        }

        // Check connection type restriction
        val connectionType = networkUtils.getConnectionTypeRestriction() // Use networkUtils
        if (connectionType == "wifi_only" && !networkUtils.isWifiConnection()) {
            Log.d(TAG, "Only allowed to download over WiFi, skipping download over mobile data")
            return@withContext
        }

        // Check circuit breaker
        if (isCircuitOpen) {
            val timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime
            if (timeSinceLastFailure < RESET_TIMEOUT) {
                Log.w(
                    TAG,
                    "Circuit breaker is open, skipping web resource download. Retry in ${(RESET_TIMEOUT - timeSinceLastFailure) / 1000} seconds"
                )
                return@withContext
            }
            isCircuitOpen = false
            failureCount = 0
            Log.d(TAG, "Circuit breaker has been reset after timeout")
        }

        var currentDelay = INITIAL_BACKOFF_DELAY
        repeat(MAX_RETRIES) { attempt ->
            try {
                // Download web resource
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = TIMEOUT_MILLIS
                connection.readTimeout = TIMEOUT_MILLIS
                connection.instanceFollowRedirects = true // Follow redirects

                // Check response code
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Invalid response code: $responseCode for URL: $url")
                }

                val outputStream = ByteArrayOutputStream()
                val inputStream = connection.inputStream
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                inputStream.close()

                val content = outputStream.toByteArray()
                if (content.isEmpty()) {
                    Log.w(TAG, "Downloaded content is empty for URL: $url")
                    // Don't throw exception, just log and potentially skip saving
                    return@withContext // Exit if content is empty
                }

                offlineDataManager.saveWebResource(url, content)

                // Reset failure count when successful
                failureCount = 0
                Log.d(TAG, "Downloaded and saved web resource for URL: $url")
                return@withContext // Exit successfully
            } catch (e: Exception) {
                failureCount++
                Log.e(
                    TAG,
                    "Error when downloading web resource $url (attempt ${attempt + 1}): ${e.message}"
                )

                if (failureCount >= FAILURE_THRESHOLD) {
                    isCircuitOpen = true
                    lastFailureTime = System.currentTimeMillis()
                    Log.w(
                        TAG,
                        "Circuit breaker is open cause too many consecutive failures while downloading web resource $url"
                    )
                    return@withContext // Exit after opening circuit breaker
                }

                // If not the last attempt, perform exponential backoff
                if (attempt < MAX_RETRIES - 1) {
                    delay(currentDelay)
                    currentDelay =
                        (currentDelay * BACKOFF_MULTIPLIER).toLong().coerceAtMost(MAX_BACKOFF_DELAY)
                }
            }
        }

        Log.e(TAG, "Tried $MAX_RETRIES times but failed to download web resource for URL: $url")
        // Throw exception or return failure state if needed after all retries fail
        // throw IOException("Failed to download resource after $MAX_RETRIES attempts: $url")
    }

    /**
     * Auto download content for a specific quiz
     */
    suspend fun autoDownloadContentForQuiz(quizId: Long) = withContext(Dispatchers.IO) {
        if (!networkUtils.isNetworkAvailable()) {
             Log.d(TAG, "Auto-download skipped for quiz $quizId: No network")
            return@withContext
        }

        try {
            val quiz = quizRepository.getQuizById(quizId) ?: return@withContext
            // Explicitly trigger sync which now includes thumbnail download
            syncQuizData(quiz)
            Log.d(TAG, "Attempted auto-download/sync for quiz $quizId")
        } catch (e: Exception) {
            Log.e(TAG, "Error during auto-download/sync for quiz $quizId: ${e.message}")
        }
    }
}

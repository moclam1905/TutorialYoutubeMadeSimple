package com.nguyenmoclam.tutorialyoutubemadesimple.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nguyenmoclam.tutorialyoutubemadesimple.data.manager.ModelDataManager
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.OpenRouterRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background worker that periodically refreshes the model data cache.
 * This ensures that the app always has recent model information without
 * requiring manual refresh by the user.
 */
@HiltWorker
class ModelDataRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val openRouterRepository: OpenRouterRepository,
    private val modelDataManager: ModelDataManager,
    private val networkUtils: NetworkUtils
) : CoroutineWorker(appContext, workerParams) {

    /**
     * Performs the background refresh of model data.
     * Checks network conditions and only refreshes when appropriate.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Check if we should refresh based on network conditions
            if (!shouldRefresh()) {
                return@withContext Result.success()
            }

            // Fetch models from repository
            try {
                val models = openRouterRepository.getAvailableModels(true)
                // Update cache with new data if successful
                modelDataManager.updateCache(models)
                return@withContext Result.success()
            } catch (e: Exception) {
                // On failure, retry unless it's a permanent error
                return@withContext if (isPermanentError(e)) {
                    Result.failure()
                } else {
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            // Retry on general exceptions
            return@withContext Result.retry()
        }
    }

    /**
     * Determines if model data should be refreshed based on network conditions.
     */
    private fun shouldRefresh(): Boolean {
        // Don't refresh on metered networks unless user allows it
        if (networkUtils.isMeteredNetwork() && !networkUtils.shouldLoadContentOnMetered()) {
            return false
        }
        
        // Don't refresh if in data saver mode
        if (networkUtils.isDataSaverEnabled()) {
            return false
        }
        
        return true
    }
    
    /**
     * Determines if an error is permanent (not worth retrying).
     */
    private fun isPermanentError(error: Throwable?): Boolean {
        return error?.message?.let { message ->
            message.contains("401") || // Unauthorized
                    message.contains("403") || // Forbidden
                    message.contains("API key not provided")
        } == true
    }
    
    companion object {
        private const val WORK_NAME = "model_data_refresh_worker"
        
        /**
         * Schedules periodic background refresh of model data.
         * 
         * @param context Application context
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
                
            val refreshRequest = PeriodicWorkRequestBuilder<ModelDataRefreshWorker>(
                    12, TimeUnit.HOURS, // Run every 12 hours
                    1, TimeUnit.HOURS  // Flex period of 1 hour
                )
                .setConstraints(constraints)
                .build()
                
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    refreshRequest
                )
        }
        
        /**
         * Cancels scheduled background refresh of model data.
         * 
         * @param context Application context
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
} 
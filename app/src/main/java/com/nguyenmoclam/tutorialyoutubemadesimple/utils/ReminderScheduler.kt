package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import com.nguyenmoclam.tutorialyoutubemadesimple.worker.ReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    private val TAG = "ReminderScheduler"

    fun scheduleReminder(quizId: Long, quizTitle: String, intervalMillis: Long) {
        Log.d(
            TAG,
            "scheduleReminder called for quizId: $quizId, title: '$quizTitle', intervalMillis: $intervalMillis"
        )
        // Cancel any existing reminder for this quiz first
        cancelReminder(quizId)

        // Create input data for the worker
        val inputData = workDataOf(
            ReminderWorker.KEY_QUIZ_ID to quizId,
            ReminderWorker.KEY_QUIZ_TITLE to quizTitle
        )

        // Create a periodic work request
        // Note: Minimum interval for periodic work is 15 minutes
        val repeatInterval = maxOf(intervalMillis, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS)
        val timeUnit = TimeUnit.MILLISECONDS // Use milliseconds for calculation
        Log.d(TAG, "Calculated repeatInterval: $repeatInterval ms")

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            repeatInterval,
            timeUnit
        ) // Use calculated interval and TimeUnit
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    // Optional: Add constraints like network required, battery not low, etc.
                    // .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(getWorkTag(quizId)) // Tag the work with quiz ID for easy cancellation
            .setInitialDelay(
                repeatInterval,
                timeUnit
            ) // Add initial delay using calculated interval
            .build()

        workManager.enqueueUniquePeriodicWork(
            getUniqueWorkName(quizId),
            ExistingPeriodicWorkPolicy.REPLACE, // Replace existing work with the same name
            reminderRequest
        )

        // OneTimeWorkRequest just for testing
        // val request = OneTimeWorkRequestBuilder<ReminderWorker>()
        //     .setInitialDelay(1, TimeUnit.MINUTES)
        //     .setInputData(inputData)
        //     .addTag(getWorkTag(quizId))
        //     .build()
        //
        // workManager.enqueueUniqueWork(
        //     getUniqueWorkName(quizId),
        //     ExistingWorkPolicy.REPLACE,
        //     request
        // )

        Log.d(
            TAG,
            "Enqueued unique periodic work with name: ${getUniqueWorkName(quizId)} and interval ${repeatInterval}ms"
        )
    }

    fun cancelReminder(quizId: Long) {
        val workName = getUniqueWorkName(quizId)
        Log.d(TAG, "cancelReminder called for quizId: $quizId, cancelling work: $workName")
        workManager.cancelUniqueWork(workName)
        // Alternatively, cancel by tag if you might have multiple workers for the same quiz (unlikely here)
        // workManager.cancelAllWorkByTag(getWorkTag(quizId))
    }

    private fun getUniqueWorkName(quizId: Long): String {
        return "quiz_reminder_${quizId}"
    }

    private fun getWorkTag(quizId: Long): String {
        return "reminder_tag_${quizId}"
    }
}

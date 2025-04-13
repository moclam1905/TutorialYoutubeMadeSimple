package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import android.content.Context
import androidx.work.*
import com.nguyenmoclam.tutorialyoutubemadesimple.worker.ReminderWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderScheduler @Inject constructor(
    context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleReminder(quizId: Long, quizTitle: String, intervalMillis: Long) {
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
        val timeUnit = TimeUnit.MILLISECONDS

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(repeatInterval, timeUnit)
            .setInputData(inputData)
            .setConstraints(Constraints.Builder()
                // Optional: Add constraints like network required, battery not low, etc.
                // .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .addTag(getWorkTag(quizId)) // Tag the work with quiz ID for easy cancellation
            .build()

        workManager.enqueueUniquePeriodicWork(
            getUniqueWorkName(quizId),
            ExistingPeriodicWorkPolicy.REPLACE, // Replace existing work with the same name
            reminderRequest
        )
    }

    fun cancelReminder(quizId: Long) {
        workManager.cancelUniqueWork(getUniqueWorkName(quizId))
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

package com.nguyenmoclam.tutorialyoutubemadesimple.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nguyenmoclam.tutorialyoutubemadesimple.MainActivity
import com.nguyenmoclam.tutorialyoutubemadesimple.R
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    // Inject repository - keep even if unused for now, might be needed later
    private val quizRepository: QuizRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_QUIZ_ID = "quiz_id"
        const val KEY_QUIZ_TITLE = "quiz_title" // Pass title directly to avoid DB query in worker
        private const val CHANNEL_ID = "quiz_reminder_channel"
        private const val NOTIFICATION_ID_BASE = 1000 // Base ID to avoid conflicts
        private const val TAG = "ReminderWorker" // Add a TAG for logging
    }

    override suspend fun doWork(): Result { // Use ListenableWorker.Result
        Log.d(TAG, "doWork started for worker with id: $id") // Log worker start
        val quizId = inputData.getLong(KEY_QUIZ_ID, -1L)
        val quizTitle = inputData.getString(KEY_QUIZ_TITLE) ?: "a quiz" // Default title
        Log.d(TAG, "Processing reminder for quizId: $quizId, title: '$quizTitle'")

        if (quizId == -1L) {
            Log.e(TAG, "Invalid quizId (-1), failing worker.")
            return Result.failure()
            return Result.failure()
        }

        try { // Wrap core logic in try-catch for better error logging
            // Create notification channel (required for Android O and above)
            createNotificationChannel()

            // Build the notification
            Log.d(TAG, "Attempting to show notification for quizId: $quizId")
            showNotification(quizId, quizTitle)

            Log.d(TAG, "doWork finished successfully for quizId: $quizId")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during doWork for quizId: $quizId", e)
            return Result.failure()
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel
        val name = applicationContext.getString(R.string.reminder_channel_name)
        val descriptionText = applicationContext.getString(R.string.reminder_channel_description)
        // Set importance to HIGH to enable heads-up display
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system; can't change the importance
        // or other notification behaviors after this
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(quizId: Long, quizTitle: String) {
        Log.d(
            TAG,
            "showNotification called for quizId: $quizId - Reverting to open MainActivity directly."
        )

        // Create a simple Intent to launch MainActivity
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            // Standard flags to bring existing task to front or start new one
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        Log.d(TAG, "Created intent for MainActivity: $intent")

        // Use FLAG_IMMUTABLE for PendingIntent
        val pendingIntentFlag =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        // Create PendingIntent using getActivity without special options
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            quizId.toInt(), // Use quizId for unique request code still
            intent,
            pendingIntentFlag
            // No ActivityOptions bundle needed
        )

        val notificationTitle = applicationContext.getString(R.string.reminder_notification_title)
        val notificationText =
            applicationContext.getString(R.string.reminder_notification_text, quizTitle)

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this drawable exists
            .setContentTitle(notificationTitle)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // Use a proper notification icon (assuming it exists/will be created)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set high priority for heads-up
            .setCategory(NotificationCompat.CATEGORY_REMINDER) // Keep category for consistency
            .setContentIntent(pendingIntent) // Standard tap action
            .setAutoCancel(true) // Dismiss notification when tapped

        // Check for notification permission before showing (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted. Cannot show notification.
                // Log this or handle based on app logic. For a worker, simply returning might be best.
                Log.w("ReminderWorker", "Notification permission not granted.")
                return // Exit the function if permission is missing
            }
        }

        // Use a unique notification ID based on quizId
        val uniqueNotificationId = NOTIFICATION_ID_BASE + quizId.toInt()
        with(NotificationManagerCompat.from(applicationContext)) {
            // Double-check permission just before notifying (though the check above should be sufficient)
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(
                    TAG,
                    "Permission granted, showing notification with id: $uniqueNotificationId for quizId: $quizId"
                )
                notify(uniqueNotificationId, builder.build())
            } else {
                Log.w(
                    TAG,
                    "Permission check failed just before notify for quizId: $quizId"
                ) // Should not happen due to earlier check, but log just in case
            }
        }
    }
}

package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import android.annotation.SuppressLint

/**
 * Utility class for time-related operations.
 */
object TimeUtils {
    /**
     * Convert a timestamp string (mm:ss) to milliseconds.
     */
    fun convertTimestampToMillis(timestamp: String): Long {
        val parts = timestamp.split(":")
        if (parts.size != 2) return 0L

        try {
            val minutes = parts[0].toInt()
            val seconds = parts[1].toInt()
            return ((minutes * 60) + seconds) * 1000L
        } catch (e: NumberFormatException) {
            return 0L
        }
    }

    /**
     * Convert milliseconds to a timestamp string (mm:ss).
     */
    @SuppressLint("DefaultLocale")
    fun convertMillisToTimestamp(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Find the closest segment based on current playback time.
     */
    fun findClosestSegmentIndex(currentTimeMillis: Long, segmentTimestamps: List<Long>): Int {
        if (segmentTimestamps.isEmpty()) return -1

        // Find the last segment that starts before or at the current time
        for (i in segmentTimestamps.indices.reversed()) {
            if (segmentTimestamps[i] <= currentTimeMillis) {
                return i
            }
        }

        // If no segment found, return the first segment
        return 0
    }
}
package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing a quiz in the database.
 * Contains basic information about each quiz created in the application.
 */
@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val quizId: Long = 0,
    
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String = "", // Store YouTube video thumbnail URL
    val language: String,
    val questionType: String,
    val questionCount: Int,
    val summaryEnabled: Boolean,
    val questionsEnabled: Boolean,
    val lastUpdated: Long, // Timestamp in milliseconds
    val localThumbnailPath: String? = null // Path to locally stored thumbnail
)

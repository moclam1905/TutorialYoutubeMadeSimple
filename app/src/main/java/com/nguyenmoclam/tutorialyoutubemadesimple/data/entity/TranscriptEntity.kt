package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity class representing a video transcript in the database.
 * Has a one-to-one relationship with QuizEntity.
 */
@Entity(
    tableName = "transcripts",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["quizId"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quizId", unique = true)]
)
data class TranscriptEntity(
    @PrimaryKey(autoGenerate = true)
    val transcriptId: Long = 0,
    
    val quizId: Long,
    val content: String,
    val language: String = "en",
    val lastUpdated: Long = System.currentTimeMillis()
)
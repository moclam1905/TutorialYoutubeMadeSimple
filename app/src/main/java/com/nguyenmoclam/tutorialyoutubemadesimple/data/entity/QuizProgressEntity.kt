package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nguyenmoclam.tutorialyoutubemadesimple.data.converter.StringMapConverter

/**
 * Entity class representing a user's progress in a quiz.
 * Stores the current question index and answered questions.
 * Has a many-to-one relationship with QuizEntity.
 */
@Entity(
    tableName = "quiz_progress",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["quizId"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quizId")]
)
@TypeConverters(StringMapConverter::class)
data class QuizProgressEntity(
    @PrimaryKey
    val quizId: Long,
    
    val currentQuestionIndex: Int = 0,
    
    // Map of question index to selected answer
    val answeredQuestions: Map<String, String> = emptyMap(),
    
    val lastUpdated: Long = System.currentTimeMillis(),
    
    // Time when the quiz was completed (0 if not completed)
    val completionTime: Long = 0
)
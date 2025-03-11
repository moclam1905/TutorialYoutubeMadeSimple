package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nguyenmoclam.tutorialyoutubemadesimple.data.converter.StringListConverter

/**
 * Entity class representing a quiz result in the database.
 * Has a many-to-one relationship with QuizEntity.
 */
@Entity(
    tableName = "quiz_results",
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
@TypeConverters(StringListConverter::class)
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true)
    val resultId: Long = 0,
    
    val quizId: Long,
    val score: Float,
    val timeTaken: Int, // Time in seconds
    val answers: List<String> // User's answers stored as a list
)
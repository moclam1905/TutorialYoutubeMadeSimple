package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nguyenmoclam.tutorialyoutubemadesimple.data.converter.StringListConverter

/**
 * Entity class representing a question in the database.
 * Has a many-to-one relationship with QuizEntity.
 */
@Entity(
    tableName = "questions",
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
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val questionId: Long = 0,
    
    val quizId: Long,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)
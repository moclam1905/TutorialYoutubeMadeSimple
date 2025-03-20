package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity class representing a key point extracted from a video in the database.
 * Has a many-to-one relationship with QuizEntity.
 */
@Entity(
    tableName = "key_points",
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
data class KeyPointEntity(
    @PrimaryKey(autoGenerate = true)
    val keyPointId: Long = 0,
    
    val quizId: Long,
    val content: String
)
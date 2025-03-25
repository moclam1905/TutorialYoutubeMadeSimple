package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nguyenmoclam.tutorialyoutubemadesimple.data.converter.StringListConverter

@Entity(
    tableName = "mindmaps",
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

@TypeConverters(StringListConverter::class)
data class MindMapEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val keyPoints: List<String>,
    val mermaidCode: String,
    val lastUpdated: Long
)

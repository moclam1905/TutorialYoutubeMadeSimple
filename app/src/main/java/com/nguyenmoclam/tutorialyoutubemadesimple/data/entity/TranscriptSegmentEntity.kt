package com.nguyenmoclam.tutorialyoutubemadesimple.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity class representing a segment of a video transcript in the database.
 * Has a many-to-one relationship with TranscriptEntity.
 */
@Entity(
    tableName = "transcript_segments",
    foreignKeys = [
        ForeignKey(
            entity = TranscriptEntity::class,
            parentColumns = ["transcriptId"],
            childColumns = ["transcriptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transcriptId")]
)
data class TranscriptSegmentEntity(
    @PrimaryKey(autoGenerate = true)
    val segmentId: Long = 0,
    
    val transcriptId: Long,
    val timestamp: String, // Format: "mm:ss"
    val timestampMillis: Long, // Timestamp in milliseconds for easier comparison
    val text: String,
    val isChapterStart: Boolean = false,
    val chapterTitle: String? = null,
    val orderIndex: Int // To maintain the order of segments
)
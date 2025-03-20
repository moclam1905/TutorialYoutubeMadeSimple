package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TranscriptEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the transcripts table.
 */
@Dao
interface TranscriptDao {
    @Query("SELECT * FROM transcripts WHERE quizId = :quizId")
    fun getTranscriptForQuiz(quizId: Long): Flow<TranscriptEntity?>

    @Query("SELECT * FROM transcripts WHERE transcriptId = :transcriptId")
    suspend fun getTranscriptById(transcriptId: Long): TranscriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscript(transcript: TranscriptEntity): Long

    @Update
    suspend fun updateTranscript(transcript: TranscriptEntity)

    @Delete
    suspend fun deleteTranscript(transcript: TranscriptEntity)

    @Query("DELETE FROM transcripts WHERE quizId = :quizId")
    suspend fun deleteTranscriptForQuiz(quizId: Long)
}
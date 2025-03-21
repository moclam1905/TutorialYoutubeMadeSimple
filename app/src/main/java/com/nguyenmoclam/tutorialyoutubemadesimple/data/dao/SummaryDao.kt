package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the summaries table.
 */
@Dao
interface SummaryDao {
    @Query("SELECT * FROM summaries WHERE quizId = :quizId")
    fun getSummaryForQuiz(quizId: Long): Flow<SummaryEntity?>

    @Query("SELECT * FROM summaries WHERE summaryId = :summaryId")
    suspend fun getSummaryById(summaryId: Long): SummaryEntity?
    
    @Query("SELECT * FROM summaries")
    fun getAllSummaries(): Flow<List<SummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: SummaryEntity): Long

    @Update
    suspend fun updateSummary(summary: SummaryEntity)

    @Delete
    suspend fun deleteSummary(summary: SummaryEntity)

    @Query("DELETE FROM summaries WHERE quizId = :quizId")
    suspend fun deleteSummaryForQuiz(quizId: Long)
}
package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the quiz results table.
 */
@Dao
interface QuizResultDao {
    @Query("SELECT * FROM quiz_results WHERE quizId = :quizId")
    fun getResultsForQuiz(quizId: Long): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE resultId = :resultId")
    suspend fun getResultById(resultId: Long): QuizResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: QuizResultEntity): Long

    @Update
    suspend fun updateResult(result: QuizResultEntity)

    @Delete
    suspend fun deleteResult(result: QuizResultEntity)

    @Query("DELETE FROM quiz_results WHERE quizId = :quizId")
    suspend fun deleteResultsForQuiz(quizId: Long)
}
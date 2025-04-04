package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the quizzes table.
 */
@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY lastUpdated DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>
    
    @Query("SELECT COUNT(*) FROM quizzes")
    suspend fun getQuizCount(): Int

    @Query("SELECT * FROM quizzes WHERE quizId = :quizId")
    suspend fun getQuizById(quizId: Long): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)

    @Query("DELETE FROM quizzes WHERE quizId = :quizId")
    suspend fun deleteQuizById(quizId: Long)

    @Query("UPDATE quizzes SET localThumbnailPath = :localPath WHERE quizId = :quizId")
    suspend fun updateLocalThumbnailPath(quizId: Long, localPath: String)
}

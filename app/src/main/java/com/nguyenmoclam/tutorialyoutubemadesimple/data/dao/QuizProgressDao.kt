package com.nguyenmoclam.tutorialyoutubemadesimple.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the quiz progress table.
 */
@Dao
interface QuizProgressDao {
    @Query("SELECT * FROM quiz_progress WHERE quizId = :quizId")
    suspend fun getProgressForQuiz(quizId: Long): QuizProgressEntity?
    
    @Query("SELECT * FROM quiz_progress WHERE quizId = :quizId")
    fun getProgressForQuizAsFlow(quizId: Long): Flow<QuizProgressEntity?>
    
    @Query("SELECT * FROM quiz_progress")
    fun getAllProgress(): Flow<List<QuizProgressEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: QuizProgressEntity): Long
    
    @Update
    suspend fun updateProgress(progress: QuizProgressEntity)
    
    @Query("DELETE FROM quiz_progress WHERE quizId = :quizId")
    suspend fun deleteProgressForQuiz(quizId: Long)
}
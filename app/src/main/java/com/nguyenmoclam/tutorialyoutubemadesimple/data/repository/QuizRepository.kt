package com.nguyenmoclam.tutorialyoutubemadesimple.data.repository

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Question
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Summary
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    suspend fun insertQuiz(quiz: Quiz): Long
    suspend fun insertSummary(summary: Summary)
    suspend fun insertQuestions(questions: List<Question>)
    suspend fun getQuizById(quizId: Long): Quiz?
    suspend fun getSummaryByQuizId(quizId: Long): Summary?
    fun getAllQuizzes(): Flow<List<Quiz>>
    suspend fun deleteQuiz(quizId: Long)
}
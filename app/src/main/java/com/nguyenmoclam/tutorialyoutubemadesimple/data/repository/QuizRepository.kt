package com.nguyenmoclam.tutorialyoutubemadesimple.data.repository

import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizProgressEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.MindMap
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Question
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Summary
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Transcript
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Topic
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    suspend fun insertQuiz(quiz: Quiz): Long
    suspend fun insertSummary(summary: Summary)
    suspend fun insertQuestions(questions: List<Question>)
    suspend fun getQuizById(quizId: Long): Quiz?
    suspend fun getSummaryByQuizId(quizId: Long): Summary?
    fun getQuestionsForQuiz(quizId: Long): Flow<List<Question>>
    fun getAllQuizzes(): Flow<List<Quiz>>
    suspend fun deleteQuiz(quizId: Long)
    suspend fun getQuizCount(): Int
    suspend fun getUsedStorageBytes(): Long

    // Quiz progress methods
    suspend fun getQuizProgressEntity(quizId: Long): QuizProgressEntity?
    fun getProgressForQuizAsFlow(quizId: Long): Flow<Map<Int, String>?>
    fun getAllProgress(): Flow<List<Map<Int, String>>>
    suspend fun saveQuizProgress(
        quizId: Long,
        currentQuestionIndex: Int,
        answeredQuestions: Map<Int, String>,
        completionTime: Long = 0
    )

    suspend fun deleteProgressForQuiz(quizId: Long)

    // Transcript methods
    suspend fun insertTranscript(transcript: Transcript)
    suspend fun getTranscriptByQuizId(quizId: Long): Transcript?
    suspend fun deleteTranscriptForQuiz(quizId: Long)

    // Topic and content question methods
    suspend fun insertTopics(topics: List<Topic>, quizId: Long)
    suspend fun getTopicsForQuiz(quizId: Long): Flow<List<Topic>>
    suspend fun deleteTopicsForQuiz(quizId: Long)

    // Key points methods
    suspend fun insertKeyPoints(keyPoints: List<String>, quizId: Long)
    suspend fun getKeyPointsForQuiz(quizId: Long): Flow<List<com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.keypoint.KeyPoint>>
    suspend fun deleteKeyPointsForQuiz(quizId: Long)

    // Mind map
    suspend fun insertMindMap(mindMap: MindMap, quizId: Long)
    suspend fun getMindMapByQuizId(quizId: Long): MindMap?
    suspend fun deleteMindMapForQuiz(quizId: Long)

}
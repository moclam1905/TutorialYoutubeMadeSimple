package com.nguyenmoclam.tutorialyoutubemadesimple.data.repository

import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.SummaryDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuestionDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizProgressDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizProgressEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.QuestionMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.QuizMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.SummaryMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Question
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val quizDao: QuizDao,
    private val summaryDao: SummaryDao,
    private val questionDao: QuestionDao,
    private val quizProgressDao: QuizProgressDao
) : QuizRepository {
    override suspend fun insertQuiz(quiz: Quiz): Long {
        return quizDao.insertQuiz(QuizMapper.toEntity(quiz))
    }

    override suspend fun insertSummary(summary: Summary) {
        summaryDao.insertSummary(SummaryMapper.toEntity(summary))
    }

    override suspend fun insertQuestions(questions: List<Question>) {
        questionDao.insertQuestions(questions.map { QuestionMapper.toEntity(it) })
    }

    override suspend fun getQuizById(quizId: Long): Quiz? {
        return quizDao.getQuizById(quizId)?.let { QuizMapper.toDomain(it) }
    }

    override suspend fun getSummaryByQuizId(quizId: Long): Summary? {
        return summaryDao.getSummaryForQuiz(quizId).first()?.let { SummaryMapper.toDomain(it) }
    }
    
    override fun getQuestionsForQuiz(quizId: Long): Flow<List<Question>> {
        return questionDao.getQuestionsForQuiz(quizId).map { questions ->
            questions.map { QuestionMapper.toDomain(it) }
        }
    }

    override fun getAllQuizzes(): Flow<List<Quiz>> {
        return quizDao.getAllQuizzes().map { quizzes -> quizzes.map { QuizMapper.toDomain(it) } }
    }

    override suspend fun deleteQuiz(quizId: Long) {
        quizDao.deleteQuizById(quizId)
    }
    
    override suspend fun getProgressForQuiz(quizId: Long): Map<Int, String>? {
        val progressEntity = quizProgressDao.getProgressForQuiz(quizId)
        return progressEntity?.answeredQuestions?.mapKeys { it.key.toInt() }
    }
    
    override fun getProgressForQuizAsFlow(quizId: Long): Flow<Map<Int, String>?> {
        return quizProgressDao.getProgressForQuizAsFlow(quizId).map { entity ->
            entity?.answeredQuestions?.mapKeys { it.key.toInt() }
        }
    }
    
    override fun getAllProgress(): Flow<List<Map<Int, String>>> {
        return quizProgressDao.getAllProgress().map { entities ->
            entities.map { entity ->
                entity.answeredQuestions.mapKeys { it.key.toInt() }
            }
        }
    }
    
    override suspend fun saveQuizProgress(quizId: Long, currentQuestionIndex: Int, answeredQuestions: Map<Int, String>) {
        // Convert Map<Int, String> to Map<String, String> for storage
        val stringKeyMap = answeredQuestions.mapKeys { it.key.toString() }
        
        val progressEntity = QuizProgressEntity(
            quizId = quizId,
            currentQuestionIndex = currentQuestionIndex,
            answeredQuestions = stringKeyMap,
            lastUpdated = System.currentTimeMillis()
        )
        
        // Check if progress already exists
        val existingProgress = quizProgressDao.getProgressForQuiz(quizId)
        if (existingProgress != null) {
            quizProgressDao.updateProgress(progressEntity)
        } else {
            quizProgressDao.insertProgress(progressEntity)
        }
    }
    
    override suspend fun deleteProgressForQuiz(quizId: Long) {
        quizProgressDao.deleteProgressForQuiz(quizId)
    }
}
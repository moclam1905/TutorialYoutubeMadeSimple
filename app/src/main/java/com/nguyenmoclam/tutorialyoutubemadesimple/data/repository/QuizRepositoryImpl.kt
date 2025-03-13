package com.nguyenmoclam.tutorialyoutubemadesimple.data.repository

import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.SummaryDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuestionDao
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
    private val questionDao: QuestionDao
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

    override fun getAllQuizzes(): Flow<List<Quiz>> {
        return quizDao.getAllQuizzes().map { quizzes -> quizzes.map { QuizMapper.toDomain(it) } }
    }

    override suspend fun deleteQuiz(quizId: Long) {
        quizDao.deleteQuizById(quizId)
    }
}
package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.topic

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Topic
import javax.inject.Inject

/**
 * Use case for saving topics to the database.
 * This use case handles the insertion of topics and their associated questions.
 */
class SaveTopicsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    /**
     * Save a list of topics for a specific quiz.
     *
     * @param topics The list of topics to save
     * @param quizId The ID of the quiz these topics belong to
     */
    suspend operator fun invoke(topics: List<Topic>, quizId: Long) {
        if (topics.isNotEmpty()) {
            quizRepository.insertTopics(topics, quizId)
        }
    }
}
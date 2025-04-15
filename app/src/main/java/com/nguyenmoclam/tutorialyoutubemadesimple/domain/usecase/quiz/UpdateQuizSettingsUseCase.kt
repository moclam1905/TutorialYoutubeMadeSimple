package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import javax.inject.Inject

class UpdateQuizSettingsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    // Add operator keyword and pass lastUpdated timestamp
    suspend operator fun invoke(quizId: Long, title: String, description: String, reminderInterval: Long?) {
        val currentTime = System.currentTimeMillis()
        // Update title and description
        quizRepository.updateQuizTitleDescription(quizId, title, description, currentTime)
        // Update reminder interval
        quizRepository.updateQuizReminderInterval(quizId, reminderInterval, currentTime)
    }
}

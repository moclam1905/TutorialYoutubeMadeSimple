package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.tag

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag.Tag
import javax.inject.Inject

/**
 * Use case to update the list of tags associated with a specific quiz.
 */
class UpdateTagsForQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    // Add suspend operator keywords
    suspend operator fun invoke(quizId: Long, tags: List<Tag>) {
        quizRepository.updateTagsForQuiz(quizId, tags)
    }
}

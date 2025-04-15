package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.tag

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.tag.Tag
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get a flow of all available tags.
 */
class GetAllTagsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    operator fun invoke(): Flow<List<Tag>> {
        return quizRepository.getAllTags()
    }
}

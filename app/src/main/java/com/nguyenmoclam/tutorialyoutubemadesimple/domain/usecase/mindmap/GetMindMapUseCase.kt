package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.mindmap

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.MindMap
import javax.inject.Inject

// (Bonus) Use case to retrieve a saved mind map from the database by quizId
class GetMindMapUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(quizId: Long): MindMap? {
        return repository.getMindMapByQuizId(quizId)
    }
}

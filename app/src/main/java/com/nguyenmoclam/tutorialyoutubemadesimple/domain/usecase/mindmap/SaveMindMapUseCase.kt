package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.mindmap

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.MindMap
import javax.inject.Inject

// Use case to save a generated mind map into the database
class SaveMindMapUseCase @Inject constructor(
    private val repository: QuizRepository  // or a specific MindMapRepository
) {
    suspend operator fun invoke(mindMap: MindMap, quizId: Long) {
        // Convert domain model to entity and insert via repository/DAO
        repository.insertMindMap(mindMap, quizId)
    }
}

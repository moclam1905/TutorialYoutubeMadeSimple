package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.progress

import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizProgressDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case to get all quiz progress entries as a map keyed by quizId.
 */
class GetAllQuizProgressMapUseCase @Inject constructor(
    private val quizProgressDao: QuizProgressDao
) {
    /**
     * Executes the use case.
     * Fetches all progress entries and converts them into a map.
     * @return A map where the key is the quizId and the value is the QuizProgressEntity.
     */
    suspend operator fun invoke(): Map<Long, QuizProgressEntity> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch the list from the Flow and then create the map
                val progressList = quizProgressDao.getAllProgress().first()
                progressList.associateBy { it.quizId }
            } catch (e: Exception) {
                // Handle potential errors, e.g., database error
                emptyMap() // Return an empty map in case of error
            }
        }
    }
}

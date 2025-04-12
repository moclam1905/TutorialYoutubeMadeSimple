package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.mindmap

import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.MindMapDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case to get the set of quiz IDs that have an associated mind map.
 */
class GetQuizIdsWithMindmapUseCase @Inject constructor(
    private val mindMapDao: MindMapDao
) {
    /**
     * Executes the use case.
     * Fetches distinct quiz IDs from the mind map table.
     * @return A set of Long containing quiz IDs that have a mind map, or an empty set if none exist or an error occurs.
     */
    suspend operator fun invoke(): Set<Long> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch the list of IDs and convert to a Set, handle null case
                mindMapDao.getAllQuizIdsWithMindmap()?.toSet() ?: emptySet()
            } catch (e: Exception) {
                // Handle potential errors, e.g., database error
                emptySet() // Return an empty set in case of error
            }
        }
    }
}

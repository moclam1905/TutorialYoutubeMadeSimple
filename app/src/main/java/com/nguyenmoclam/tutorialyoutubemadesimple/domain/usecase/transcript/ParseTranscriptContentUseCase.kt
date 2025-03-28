package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.transcript

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for parsing transcript content into segments with improved performance for long transcripts.
 * Implements chunking to process large transcripts in smaller pieces and uses Flow for streaming results.
 */
class ParseTranscriptContentUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    // Cache to avoid reprocessing the same content
    private val contentCache = mutableMapOf<String, List<TranscriptSegment>>()

    /**
     * Process transcript content and return segments as a list.
     * This method is backward compatible with existing code.
     */
    suspend operator fun invoke(content: String, transcriptId: Long): List<TranscriptSegment> {
        // Check cache first
        val cacheKey = "$transcriptId:${content.hashCode()}"
        contentCache[cacheKey]?.let { return it }

        // If not in cache, process the content
        val segments = quizRepository.parseTranscriptContent(content, transcriptId)

        // Cache the result
        if (segments.isNotEmpty()) {
            contentCache[cacheKey] = segments
        }

        return segments
    }

    /**
     * Process transcript content in chunks and emit segments as a Flow.
     * This allows for progressive UI updates and reduces memory pressure.
     *
     * @param content The transcript content to parse
     * @param transcriptId The ID of the transcript
     * @param chunkSize The size of each chunk to process (number of lines)
     * @return Flow of TranscriptSegment lists that are emitted as they're processed
     */
    fun invokeAsFlow(
        content: String,
        transcriptId: Long,
        chunkSize: Int = 100
    ): Flow<List<TranscriptSegment>> = flow {
        // Check cache first
        val cacheKey = "$transcriptId:${content.hashCode()}"
        contentCache[cacheKey]?.let {
            emit(it)
            return@flow
        }

        // Split content into chunks for processing
        val lines = content.split("\n")
        val chunks = lines.chunked(chunkSize)

        val allSegments = mutableListOf<TranscriptSegment>()

        for (chunk in chunks) {
            val chunkContent = chunk.joinToString("\n")
            val chunkSegments = quizRepository.parseTranscriptContent(chunkContent, transcriptId)

            if (chunkSegments.isNotEmpty()) {
                allSegments.addAll(chunkSegments)
                emit(allSegments.toList()) // Emit current progress
            }
        }

        // Process the segments for chapters after all chunks are processed
        if (allSegments.isNotEmpty()) {
            val processedSegments = processSegmentsForChapters(allSegments, transcriptId)
            emit(processedSegments)

            // Cache the final result
            contentCache[cacheKey] = processedSegments
        }
    }

    /**
     * Process segments to identify chapters after all chunks are processed.
     * This is needed because chapter detection requires analyzing the entire transcript.
     *
     * Note: This method is now a fallback for when chapters aren't already identified from YouTube.
     * It will only attempt to detect chapters if none are already present in the segments.
     */
    private fun processSegmentsForChapters(
        segments: List<TranscriptSegment>,
        transcriptId: Long
    ): List<TranscriptSegment> {
        // If segments already have chapters (likely from YouTube API), don't try to detect more
        // to avoid conflicting chapter structures
        if (segments.any { it.isChapterStart }) {
            return segments
        }

        // If no chapters were detected and we have enough segments, try to infer them from significant time gaps
        if (segments.size > 3) {
            // Find segments with significant time gaps and mark them as chapter starts
            return segments.mapIndexed { index, segment ->
                if (index > 0) {
                    val previousSegment = segments[index - 1]
                    val gap = segment.timestampMillis - previousSegment.timestampMillis

                    // If gap is significant (> 30 seconds), mark as chapter start
                    if (gap > 30000) {
                        segment.copy(
                            isChapterStart = true,
                            chapterTitle = segment.text.take(50)
                                .trim() // Use beginning of text as title
                        )
                    } else {
                        segment
                    }
                } else if (index == 0) {
                    // Mark first segment as chapter start
                    segment.copy(
                        isChapterStart = true,
                        chapterTitle = "Introduction"
                    )
                } else {
                    segment
                }
            }
        }

        return segments
    }

    /**
     * Clear the cache when needed (e.g., when memory pressure is high)
     */
    fun clearCache() {
        contentCache.clear()
    }
}
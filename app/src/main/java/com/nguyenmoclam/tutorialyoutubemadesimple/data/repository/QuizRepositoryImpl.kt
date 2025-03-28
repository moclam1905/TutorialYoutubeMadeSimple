package com.nguyenmoclam.tutorialyoutubemadesimple.data.repository

import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.ContentQuestionDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.KeyPointDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.MindMapDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.SummaryDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuestionDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizProgressDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.TopicDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.TranscriptDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.TranscriptSegmentDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizProgressEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.ContentQuestionMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.KeyPointMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.MindMapMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.QuestionMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.QuizMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.SummaryMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.TopicMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.TranscriptMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.data.mapper.TranscriptSegmentMapper
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.MindMap
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Question
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Quiz
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Summary
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Transcript
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.TranscriptSegment
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Topic
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.map

class QuizRepositoryImpl @Inject constructor(
    private val quizDao: QuizDao,
    private val summaryDao: SummaryDao,
    private val questionDao: QuestionDao,
    private val quizProgressDao: QuizProgressDao,
    private val transcriptDao: TranscriptDao,
    private val topicDao: TopicDao,
    private val contentQuestionDao: ContentQuestionDao,
    private val keyPointDao: KeyPointDao,
    private val mindMapDao: MindMapDao,
    private val transcriptSegmentDao: TranscriptSegmentDao
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

    override fun getQuestionsForQuiz(quizId: Long): Flow<List<Question>> {
        return questionDao.getQuestionsForQuiz(quizId).map { questions ->
            questions.map { QuestionMapper.toDomain(it) }
        }
    }

    override fun getAllQuizzes(): Flow<List<Quiz>> {
        return quizDao.getAllQuizzes().map { quizzes -> quizzes.map { QuizMapper.toDomain(it) } }
    }

    override suspend fun deleteQuiz(quizId: Long) {
        quizDao.deleteQuizById(quizId)
    }

    override suspend fun getQuizProgressEntity(quizId: Long): QuizProgressEntity? {
        return quizProgressDao.getProgressForQuiz(quizId)
    }

    override fun getProgressForQuizAsFlow(quizId: Long): Flow<Map<Int, String>?> {
        return quizProgressDao.getProgressForQuizAsFlow(quizId).map { entity ->
            entity?.answeredQuestions?.mapKeys { it.key.toInt() }
        }
    }

    override fun getAllProgress(): Flow<List<Map<Int, String>>> {
        return quizProgressDao.getAllProgress().map { entities ->
            entities.map { entity ->
                entity.answeredQuestions.mapKeys { it.key.toInt() }
            }
        }
    }

    override suspend fun saveQuizProgress(
        quizId: Long,
        currentQuestionIndex: Int,
        answeredQuestions: Map<Int, String>,
        completionTime: Long
    ) {
        // Convert Map<Int, String> to Map<String, String> for storage
        val stringKeyMap = answeredQuestions.mapKeys { it.key.toString() }

        // Check if progress already exists to preserve completion time if not provided
        val existingProgress = quizProgressDao.getProgressForQuiz(quizId)
        val finalCompletionTime = if (completionTime > 0) {
            completionTime
        } else {
            // Otherwise, preserve the existing completion time if available
            existingProgress?.completionTime ?: 0L
        }

        val progressEntity = QuizProgressEntity(
            quizId = quizId,
            currentQuestionIndex = currentQuestionIndex,
            answeredQuestions = stringKeyMap,
            lastUpdated = System.currentTimeMillis(),
            completionTime = finalCompletionTime
        )

        if (existingProgress != null) {
            quizProgressDao.updateProgress(progressEntity)
        } else {
            quizProgressDao.insertProgress(progressEntity)
        }
    }

    override suspend fun deleteProgressForQuiz(quizId: Long) {
        quizProgressDao.deleteProgressForQuiz(quizId)
    }

    // Transcript methods
    override suspend fun insertTranscript(transcript: Transcript): Long {
        return transcriptDao.insertTranscript(TranscriptMapper.toEntity(transcript))
    }

    override suspend fun getTranscriptByQuizId(quizId: Long): Transcript? {
        return transcriptDao.getTranscriptForQuiz(quizId).first()
            ?.let { TranscriptMapper.toDomain(it) }
    }

    override suspend fun deleteTranscriptForQuiz(quizId: Long) {
        transcriptDao.deleteTranscriptForQuiz(quizId)
    }

    // Topic and content question methods
    override suspend fun insertTopics(topics: List<Topic>, quizId: Long) {
        // Insert topics first to get their IDs
        val topicIds = topicDao.insertTopics(topics.map { TopicMapper.toEntity(it, quizId) })

        // Insert questions for each topic
        topics.forEachIndexed { index, topic ->
            val topicId = topicIds[index]
            val questions = topic.questions
            if (questions.isNotEmpty()) {
                contentQuestionDao.insertQuestions(questions.map {
                    ContentQuestionMapper.toEntity(
                        it,
                        topicId
                    )
                })
            }
        }
    }

    override suspend fun getTopicsForQuiz(quizId: Long): Flow<List<Topic>> {
        return topicDao.getTopicsForQuiz(quizId).map { topicEntities ->
            topicEntities.map { topicEntity ->
                // For each topic, load its questions
                val questions = contentQuestionDao.getQuestionsForTopic(topicEntity.topicId).first()
                    .map { ContentQuestionMapper.toDomain(it) }

                // Create the domain model with the questions
                TopicMapper.toDomain(topicEntity, questions)
            }
        }
    }

    override suspend fun deleteTopicsForQuiz(quizId: Long) {
        // This will cascade delete all related content questions due to foreign key constraints
        topicDao.deleteTopicsForQuiz(quizId)
    }

    // Key points methods implementation
    override suspend fun insertKeyPoints(keyPoints: List<String>, quizId: Long) {
        keyPointDao.insertKeyPoints(keyPoints.map { KeyPointMapper.toEntity(it, quizId) })
    }

    override suspend fun getKeyPointsForQuiz(quizId: Long): Flow<List<com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.keypoint.KeyPoint>> {
        return keyPointDao.getKeyPointsForQuiz(quizId).map { entities ->
            entities.map { KeyPointMapper.toDomain(it) }
        }
    }

    override suspend fun deleteKeyPointsForQuiz(quizId: Long) {
        keyPointDao.deleteKeyPointsForQuiz(quizId)
    }

    override suspend fun insertMindMap(
        mindMap: MindMap,
        quizId: Long
    ) {
        // Ensure the mindMap has the correct quizId
        val mindMapWithQuizId = if (mindMap.quizId != quizId) {
            mindMap.copy(quizId = quizId)
        } else {
            mindMap
        }

        // Convert domain model to entity and insert
        val entity = MindMapMapper.toEntity(mindMapWithQuizId)
        mindMapDao.insertMindMap(entity)
    }

    override suspend fun getMindMapByQuizId(quizId: Long): MindMap? {
        // Get the entity from DAO and convert to domain model if not null
        return mindMapDao.getMindMapForQuiz(quizId)?.let { entity ->
            MindMapMapper.toDomain(entity)
        }
    }

    override suspend fun deleteMindMapForQuiz(quizId: Long) {
        // Delete the mind map for the given quiz ID
        mindMapDao.deleteMindMapForQuiz(quizId)
    }

    override suspend fun getQuizCount(): Int {
        return quizDao.getQuizCount()
    }

    override suspend fun getUsedStorageBytes(): Long {
        var totalBytes: Long = 0

        try {
            // Calculate size of quizzes table
            val quizzes = quizDao.getAllQuizzes().first()
            quizzes.forEach { quiz ->
                // Add fixed overhead for each entity (IDs, timestamps, etc.)
                totalBytes += 40 // Estimated overhead per entity

                // Calculate string fields
                totalBytes += (quiz.title.length + quiz.description.length + quiz.videoUrl.length +
                        quiz.thumbnailUrl.length + quiz.language.length + quiz.questionType.length) * 2 // UTF-16 encoding
            }

            // Calculate size of questions table
            val questions = questionDao.getAllQuestions().first()
            questions.forEach { question ->
                totalBytes += 32 // Estimated overhead
                totalBytes += question.questionText.length * 2
                question.options.forEach { option ->
                    totalBytes += option.length * 2
                }
                totalBytes += question.correctAnswer.length * 2
            }

            // Calculate size of summaries table
            val summaries = summaryDao.getAllSummaries().first()
            summaries.forEach { summary ->
                totalBytes += 24 // Estimated overhead
                totalBytes += summary.content.length * 2
            }

            // Calculate size of transcripts table
            val transcripts = transcriptDao.getAllTranscripts().first()
            transcripts.forEach { transcript ->
                totalBytes += 32 // Estimated overhead
                totalBytes += transcript.content.length * 2
            }

            // Calculate size of key_points table
            val keyPoints = keyPointDao.getAllKeyPoints().first()
            keyPoints.forEach { keyPoint ->
                totalBytes += 24 // Estimated overhead
                totalBytes += keyPoint.content.length * 2
            }

            // Calculate size of topics table
            val topics = topicDao.getAllTopics().first()
            topics.forEach { topic ->
                totalBytes += 32 // Estimated overhead
                totalBytes += (topic.title.length + topic.rephrasedTitle.length) * 2
            }

            // Calculate size of content_questions table
            val contentQuestions = contentQuestionDao.getAllQuestions().first()
            contentQuestions.forEach { question ->
                totalBytes += 40 // Estimated overhead
                totalBytes += (question.original.length + question.rephrased.length + question.answer.length) * 2
            }

            // Add database overhead (indices, metadata, etc.)
            totalBytes = (totalBytes * 1.2).toLong() // Add 20% for database overhead
        } catch (e: Exception) {
            e.printStackTrace()
            // Return a fallback value or the last calculated value
        }

        return totalBytes
    }

    override fun getTranscriptForQuiz(quizId: Long): Flow<Transcript?> {
        return transcriptDao.getTranscriptForQuiz(quizId).map { entity ->
            entity?.let { TranscriptMapper.toDomain(it) }
        }
    }

    override fun getSegmentsForTranscript(transcriptId: Long): Flow<List<TranscriptSegment>> {
        return transcriptSegmentDao.getSegmentsForTranscript(transcriptId).map { entities ->
            entities.map { TranscriptSegmentMapper.fromEntity(it) }
        }
    }

    override fun getChaptersForTranscript(transcriptId: Long): Flow<List<TranscriptSegment>> {
        return transcriptSegmentDao.getChaptersForTranscript(transcriptId).map { entities ->
            entities.map { TranscriptSegmentMapper.fromEntity(it) }
        }
    }

    override suspend fun getCurrentSegment(
        transcriptId: Long,
        currentTimeMillis: Long
    ): TranscriptSegment? {
        return transcriptSegmentDao.getCurrentSegment(transcriptId, currentTimeMillis)?.let {
            TranscriptSegmentMapper.fromEntity(it)
        }
    }

    override suspend fun saveTranscriptWithSegments(
        transcript: Transcript,
        segments: List<TranscriptSegment>
    ): Long {
        // Insert transcript first
        val transcriptId = transcriptDao.insertTranscript(TranscriptMapper.toEntity(transcript))

        // Process segments to identify potential chapters if not already marked
        val processedSegments = processSegmentsForChapters(segments)
        
        // Insert segments with the transcript ID
        val segmentEntities = processedSegments.mapIndexed { index, segment ->
            TranscriptSegmentMapper.toEntity(segment.copy(transcriptId = transcriptId))
                .copy(orderIndex = index)
        }
        transcriptSegmentDao.insertSegments(segmentEntities)

        return transcriptId
    }
    
    /**
     * Process segments to identify potential chapters based on text content patterns.
     * This helps ensure chapters are properly marked even if they weren't identified during initial parsing.
     * 
     * Note: This method is now a fallback for when chapters aren't already identified from YouTube.
     * It will only process segments that aren't already marked as chapters.
     */
    private fun processSegmentsForChapters(segments: List<TranscriptSegment>): List<TranscriptSegment> {
        // If segments list is empty, return as is
        if (segments.isEmpty()) return segments
        
        // If we already have chapters from YouTube API, prioritize those and only process unmarked segments
        val hasExistingChapters = segments.any { it.isChapterStart }
        
        return segments.mapIndexed { index, segment ->
            // Skip if already marked as chapter
            if (segment.isChapterStart) return@mapIndexed segment
            
            // If we already have chapters from YouTube, be more conservative about adding new ones
            // to avoid conflicting chapter structures
            if (hasExistingChapters) {
                // Only look for explicit chapter markers in text when we already have some chapters
                val chapterRegex = "\\[(.+?)\\]".toRegex()
                val chapterMatch = chapterRegex.find(segment.text)
                
                if (chapterMatch != null) {
                    // Extract chapter title
                    val chapterTitle = chapterMatch.groupValues[1].trim()
                    
                    // Create new segment with chapter information
                    segment.copy(
                        isChapterStart = true,
                        chapterTitle = chapterTitle,
                        // Optionally clean the text by removing the chapter marker
                        text = segment.text.replace(chapterMatch.value, "").trim()
                    )
                } else {
                    segment
                }
            } else {
                // More aggressive chapter detection when no chapters from YouTube
                // Check for chapter patterns in text
                val chapterRegex = "\\[(.+?)\\]".toRegex()
                val chapterMatch = chapterRegex.find(segment.text)
                
                if (chapterMatch != null) {
                    // Extract chapter title
                    val chapterTitle = chapterMatch.groupValues[1].trim()
                    
                    // Create new segment with chapter information
                    segment.copy(
                        isChapterStart = true,
                        chapterTitle = chapterTitle,
                        // Optionally clean the text by removing the chapter marker
                        text = segment.text.replace(chapterMatch.value, "").trim()
                    )
                } else {
                    // Check for other common chapter indicators
                    val isLikelyChapter = segment.text.startsWith("Chapter", ignoreCase = true) ||
                            segment.text.startsWith("Section", ignoreCase = true) ||
                            (index > 0 && segment.timestampMillis - segments[index-1].timestampMillis > 30000) // Gap > 30s might indicate chapter
                    
                    if (isLikelyChapter) {
                        segment.copy(
                            isChapterStart = true,
                            chapterTitle = segment.text.take(50) // Use beginning of text as title if no explicit title
                        )
                    } else {
                        segment
                    }
                }
            }
        }
    }

    override suspend fun updateTranscript(transcript: Transcript) {
        transcriptDao.updateTranscript(TranscriptMapper.toEntity(transcript))
    }

    override suspend fun deleteTranscript(transcriptId: Long) {
        // Room will handle cascade deletion of segments due to foreign key constraints
        transcriptDao.getTranscriptById(transcriptId)?.let {
            transcriptDao.deleteTranscript(it)
        }
    }

    override suspend fun parseTranscriptContent(
        content: String,
        transcriptId: Long
    ): List<TranscriptSegment> {
        // Parse the raw transcript content into segments
        val segments = mutableListOf<TranscriptSegment>()
        val lines = content.split("\n")

        var currentTimestamp = ""
        var currentText = ""
        var isChapterStart = false
        var chapterTitle: String? = null
        var segmentIndex = 0
        var lastTimestampMillis = 0L

        // First pass: detect explicit chapter markers and timestamps
        for (line in lines) {
            val trimmedLine = line.trim()

            // Skip empty lines
            if (trimmedLine.isEmpty()) continue

            // Check if this line is a timestamp
            val timestampRegex = "\\d{1,2}:\\d{2}".toRegex()
            val timestampMatch = timestampRegex.find(trimmedLine)

            if (timestampMatch != null) {
                // If we already have a timestamp and text, save the previous segment
                if (currentTimestamp.isNotEmpty() && currentText.isNotEmpty()) {
                    val timestampMillis = TimeUtils.convertTimestampToMillis(currentTimestamp)
                    
                    // Check for significant time gap (potential chapter boundary)
                    if (lastTimestampMillis > 0 && timestampMillis - lastTimestampMillis > 30000) { // 30 seconds gap
                        isChapterStart = true
                        if (chapterTitle == null) {
                            // Use first part of text as chapter title if none is explicitly defined
                            chapterTitle = currentText.take(50).trim()
                        }
                    }
                    
                    segments.add(
                        TranscriptSegment(
                            transcriptId = transcriptId,
                            timestamp = currentTimestamp,
                            timestampMillis = timestampMillis,
                            text = currentText.trim(),
                            isChapterStart = isChapterStart,
                            chapterTitle = chapterTitle
                        )
                    )
                    
                    lastTimestampMillis = timestampMillis

                    // Reset for the next segment
                    isChapterStart = false
                    chapterTitle = null
                    segmentIndex++
                }

                // Extract the timestamp and text
                currentTimestamp = timestampMatch.value
                currentText = trimmedLine.substring(timestampMatch.range.last + 1).trim()

                // Check for explicit chapter markers
                // 1. Text in brackets [Chapter Title]
                val bracketChapterRegex = "\\[(.+?)\\]".toRegex()
                val bracketChapterMatch = bracketChapterRegex.find(currentText)
                
                // 2. Text starting with "Chapter" or similar keywords
                val keywordChapterRegex = "^(Chapter|Section|Part|Topic)\\s+\\d+:?\\s*(.+)".toRegex(RegexOption.IGNORE_CASE)
                val keywordChapterMatch = keywordChapterRegex.find(currentText)

                when {
                    bracketChapterMatch != null -> {
                        isChapterStart = true
                        chapterTitle = bracketChapterMatch.groupValues[1].trim()
                        // Remove the chapter title from the text
                        currentText = currentText.replace(bracketChapterMatch.value, "").trim()
                    }
                    keywordChapterMatch != null -> {
                        isChapterStart = true
                        chapterTitle = keywordChapterMatch.value.trim()
                    }
                    // Check for timestamps at the beginning of videos (often chapter markers)
                    segmentIndex == 0 -> {
                        isChapterStart = true
                        chapterTitle = "Introduction"
                    }
                }
            } else {
                // This line is a continuation of the current text
                if (currentText.isNotEmpty()) {
                    currentText += " " + trimmedLine
                } else {
                    currentText = trimmedLine
                }
                
                // Check if this continuation line contains chapter information
                if (!isChapterStart) {
                    val continuationChapterRegex = "^(Chapter|Section|Part|Topic)\\s+\\d+:?\\s*(.+)".toRegex(RegexOption.IGNORE_CASE)
                    if (continuationChapterRegex.containsMatchIn(trimmedLine)) {
                        isChapterStart = true
                        chapterTitle = trimmedLine.trim()
                    }
                }
            }
        }

        // Add the last segment if there's any
        if (currentTimestamp.isNotEmpty() && currentText.isNotEmpty()) {
            val timestampMillis = TimeUtils.convertTimestampToMillis(currentTimestamp)
            segments.add(
                TranscriptSegment(
                    transcriptId = transcriptId,
                    timestamp = currentTimestamp,
                    timestampMillis = timestampMillis,
                    text = currentText.trim(),
                    isChapterStart = isChapterStart,
                    chapterTitle = chapterTitle
                )
            )
        }
        
        // Second pass: If no chapters were detected, try to infer them from significant time gaps
        if (segments.none { it.isChapterStart } && segments.size > 3) {
            // Find segments with significant time gaps and mark them as chapter starts
            val processedSegments = segments.mapIndexed { index, segment ->
                if (index > 0) {
                    val previousSegment = segments[index - 1]
                    val gap = segment.timestampMillis - previousSegment.timestampMillis
                    
                    // If gap is significant (> 30 seconds), mark as chapter start
                    if (gap > 30000) {
                        segment.copy(
                            isChapterStart = true,
                            chapterTitle = segment.text.take(50).trim() // Use beginning of text as title
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
            return processedSegments
        }

        return segments
    }
}
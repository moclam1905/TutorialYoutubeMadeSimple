package com.nguyenmoclam.tutorialyoutubemadesimple.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nguyenmoclam.tutorialyoutubemadesimple.data.converter.StringListConverter
import com.nguyenmoclam.tutorialyoutubemadesimple.data.converter.StringMapConverter
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.ContentQuestionDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.KeyPointDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.MindMapDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuestionDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizProgressDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizResultDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.SummaryDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.TopicDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.TranscriptDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.ContentQuestionEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.KeyPointEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.MindMapEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuestionEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizProgressEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizResultEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.SummaryEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TopicEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.TranscriptEntity

/**
 * Main database configuration for the application.
 * This abstract class defines the database schema and provides access to DAOs.
 */
@Database(
    entities = [
        QuizEntity::class,
        QuestionEntity::class,
        QuizResultEntity::class,
        SummaryEntity::class,
        QuizProgressEntity::class,
        TranscriptEntity::class,
        TopicEntity::class,
        ContentQuestionEntity::class,
        KeyPointEntity::class,
        MindMapEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(StringListConverter::class, StringMapConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun summaryDao(): SummaryDao
    abstract fun quizProgressDao(): QuizProgressDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun keyPointDao(): KeyPointDao
    abstract fun topicDao(): TopicDao
    abstract fun contentQuestionDao(): ContentQuestionDao
    abstract fun mindMapDao(): MindMapDao

    companion object {
        const val DATABASE_NAME = "youtube_tutorial_app_db"
    }
}
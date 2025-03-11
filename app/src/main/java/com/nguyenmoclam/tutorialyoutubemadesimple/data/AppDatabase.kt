package com.nguyenmoclam.tutorialyoutubemadesimple.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nguyenmoclam.tutorialyoutubemadesimple.data.converter.StringListConverter
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuestionDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizResultDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.SummaryDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuestionEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.QuizResultEntity
import com.nguyenmoclam.tutorialyoutubemadesimple.data.entity.SummaryEntity

/**
 * Main database configuration for the application.
 * This abstract class defines the database schema and provides access to DAOs.
 */
@Database(
    entities = [
        QuizEntity::class,
        QuestionEntity::class,
        QuizResultEntity::class,
        SummaryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun summaryDao(): SummaryDao

    companion object {
        const val DATABASE_NAME = "youtube_tutorial_app_db"
    }
}
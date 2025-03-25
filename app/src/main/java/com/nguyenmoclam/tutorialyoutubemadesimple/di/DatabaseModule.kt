package com.nguyenmoclam.tutorialyoutubemadesimple.di

import android.content.Context
import androidx.room.Room
import com.nguyenmoclam.tutorialyoutubemadesimple.data.AppDatabase
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides database-related dependencies.
 * This module is installed in the SingletonComponent, ensuring
 * that provided dependencies exist for the entire application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance.
     * 
     * @param context The application context
     * @return The AppDatabase instance
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    /**
     * Provides the QuizDao instance.
     * 
     * @param database The AppDatabase instance
     * @return The QuizDao instance
     */
    @Provides
    fun provideQuizDao(database: AppDatabase): QuizDao {
        return database.quizDao()
    }

    /**
     * Provides the QuestionDao instance.
     * 
     * @param database The AppDatabase instance
     * @return The QuestionDao instance
     */
    @Provides
    fun provideQuestionDao(database: AppDatabase): QuestionDao {
        return database.questionDao()
    }

    /**
     * Provides the QuizResultDao instance.
     * 
     * @param database The AppDatabase instance
     * @return The QuizResultDao instance
     */
    @Provides
    fun provideQuizResultDao(database: AppDatabase): QuizResultDao {
        return database.quizResultDao()
    }

    /**
     * Provides the SummaryDao instance.
     * 
     * @param database The AppDatabase instance
     * @return The SummaryDao instance
     */
    @Provides
    fun provideSummaryDao(database: AppDatabase): SummaryDao {
        return database.summaryDao()
    }
    
    /**
     * Provides the QuizProgressDao instance.
     * 
     * @param database The AppDatabase instance
     * @return The QuizProgressDao instance
     */
    @Provides
    fun provideQuizProgressDao(database: AppDatabase): QuizProgressDao {
        return database.quizProgressDao()
    }
    
    /**
     * Provides the TranscriptDao instance.
     * 
     * @param database The AppDatabase instance
     * @return The TranscriptDao instance
     */
    @Provides
    fun provideTranscriptDao(database: AppDatabase): TranscriptDao {
        return database.transcriptDao()
    }

    /**
     * Provides the TopicDao instance.
     * 
     * @param database The AppDatabase instance
     * @return The TopicDao instance
     */
    @Provides
    fun provideTopicDao(database: AppDatabase): TopicDao {
        return database.topicDao()
    }

    /**
     * Provides the ContentQuestionDao instance.
     * 
     * @param database The AppDatabase instance
     * @return The ContentQuestionDao instance
     */
    @Provides
    fun provideContentQuestionDao(database: AppDatabase): ContentQuestionDao {
        return database.contentQuestionDao()
    }

    @Provides
    fun provideKeyPointDao(database: AppDatabase): KeyPointDao {
        return database.keyPointDao()
    }

    @Provides
    fun provideMindMapDao(database: AppDatabase): MindMapDao {
        return database.mindMapDao()
    }

}
package com.nguyenmoclam.tutorialyoutubemadesimple.di

import android.content.Context
import androidx.room.Room
import com.nguyenmoclam.tutorialyoutubemadesimple.data.AppDatabase
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuestionDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.QuizResultDao
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.SummaryDao
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
}
package com.nguyenmoclam.tutorialyoutubemadesimple.di

import com.nguyenmoclam.tutorialyoutubemadesimple.data.AppDatabase
import com.nguyenmoclam.tutorialyoutubemadesimple.data.dao.TokenUsageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing usage monitoring dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object UsageModule {
    /**
     * Provides the TokenUsageDao for monitoring token usage.
     */
    @Provides
    @Singleton
    fun provideTokenUsageDao(database: AppDatabase): TokenUsageDao {
        return database.tokenUsageDao()
    }
} 
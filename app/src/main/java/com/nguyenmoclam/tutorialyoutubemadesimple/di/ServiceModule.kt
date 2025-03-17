package com.nguyenmoclam.tutorialyoutubemadesimple.di

import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.YouTubeTranscriptLight
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides service-related dependencies.
 * This module is installed in the SingletonComponent, ensuring
 * that provided dependencies exist for the entire application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    /**
     * Provides the LLMProcessor instance.
     * 
     * @return The LLMProcessor instance
     */
    @Provides
    @Singleton
    fun provideLLMProcessor(): LLMProcessor {
        return LLMProcessor()
    }

    /**
     * Provides the YouTubeTranscriptLight instance.
     * 
     * @return The YouTubeTranscriptLight instance
     */
    @Provides
    @Singleton
    fun provideYouTubeTranscriptLight(): YouTubeTranscriptLight {
        return YouTubeTranscriptLight()
    }
}
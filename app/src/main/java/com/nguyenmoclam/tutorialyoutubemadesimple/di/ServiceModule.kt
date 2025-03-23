package com.nguyenmoclam.tutorialyoutubemadesimple.di

import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterApi
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.YouTubeTranscriptLight
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
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
     * @param networkUtils The NetworkUtils instance for checking network conditions
     * @return The LLMProcessor instance
     */
    @Provides
    @Singleton
    fun provideLLMProcessor(
        networkUtils: NetworkUtils,
        openRouterApi: OpenRouterApi
    ): LLMProcessor {
        return LLMProcessor(networkUtils, openRouterApi)
    }

    /**
     * Provides the YouTubeTranscriptLight instance.
     *
     * @return The YouTubeTranscriptLight instance
     */
    @Provides
    @Singleton
    fun provideYouTubeTranscriptLight(networkUtils: NetworkUtils): YouTubeTranscriptLight {
        return YouTubeTranscriptLight(networkUtils)
    }
}
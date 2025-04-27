package com.nguyenmoclam.tutorialyoutubemadesimple.di

import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterApi
import com.nguyenmoclam.tutorialyoutubemadesimple.data.model.openrouter.LLMConfig
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.UsageRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.YouTubeTranscriptLight
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.SecurePreferences
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
     * @param openRouterApi The OpenRouterApi instance for making API calls
     * @param securePreferences The SecurePreferences instance for secure API key storage
     * @param usageRepository The UsageRepository instance for managing usage data
     * @return The LLMProcessor instance
     */
    @Provides
    @Singleton
    fun provideLLMProcessor(
        networkUtils: NetworkUtils,
        openRouterApi: OpenRouterApi,
        securePreferences: SecurePreferences,
        usageRepository: UsageRepository
    ): LLMProcessor {
        return LLMProcessor(networkUtils, openRouterApi, securePreferences, usageRepository, null)
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
package com.nguyenmoclam.tutorialyoutubemadesimple.di

import android.content.Context
import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterApi
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.UsageRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.UserDataRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import com.nguyenmoclam.tutorialyoutubemadesimple.lib.YouTubeTranscriptLight
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
     * @param userDataRepository The UserDataRepository instance for shared user state
     * @param context The application context for error handling
     * @return The LLMProcessor instance
     */
    @Provides
    @Singleton
    fun provideLLMProcessor(
        networkUtils: NetworkUtils,
        openRouterApi: OpenRouterApi,
        securePreferences: SecurePreferences,
        usageRepository: UsageRepository,
        userDataRepository: UserDataRepository,
        @ApplicationContext context: Context
    ): LLMProcessor {
        return LLMProcessor(
            networkUtils = networkUtils, 
            openRouterApi = openRouterApi, 
            securePreferences = securePreferences, 
            usageRepository = usageRepository, 
            userDataRepository = userDataRepository,
            initialConfig = null, 
            context = context
        )
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
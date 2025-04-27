package com.nguyenmoclam.tutorialyoutubemadesimple.di

import android.content.Context
import com.nguyenmoclam.tutorialyoutubemadesimple.data.manager.ModelDataManager
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.OpenRouterRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.service.OpenRouterService
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.ApiKeyValidator
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing model data management components.
 * This module handles the efficient caching, indexing, and processing
 * of model data throughout the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object ModelDataModule {
    
    /**
     * Provides the ModelDataManager instance.
     * This component handles efficient caching and indexing of model data.
     * 
     * @param context The application context
     * @param networkUtils Network utilities for connectivity checks
     * @return The ModelDataManager instance
     */
    @Provides
    @Singleton
    fun provideModelDataManager(
        @ApplicationContext context: Context,
        networkUtils: NetworkUtils
    ): ModelDataManager {
        return ModelDataManager(context, networkUtils)
    }
    
    /**
     * Provides the updated OpenRouterRepository instance with ModelDataManager integration.
     * This method overrides the provider from RepositoryModule with our enhanced implementation.
     * 
     * @param openRouterService Service for OpenRouter API communication
     * @param securePreferences Secure storage for sensitive data
     * @param apiKeyValidator Validator for API keys
     * @param modelDataManager Model data caching and indexing manager
     * @return The enhanced OpenRouterRepository instance
     */
    @Provides
    @Singleton
    fun provideOpenRouterRepository(
        openRouterService: OpenRouterService,
        securePreferences: SecurePreferences,
        apiKeyValidator: ApiKeyValidator,
        modelDataManager: ModelDataManager
    ): OpenRouterRepository {
        return OpenRouterRepository(
            openRouterService, 
            securePreferences, 
            apiKeyValidator,
            modelDataManager
        )
    }
} 
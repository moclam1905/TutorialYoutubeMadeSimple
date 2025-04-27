package com.nguyenmoclam.tutorialyoutubemadesimple.di

import android.content.Context
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepositoryImpl
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.SettingsRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.SettingsRepositoryImpl
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkStateListener
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.OfflineDataManager
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.OfflineSyncManager
import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterApi
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.OpenRouterRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.service.OpenRouterService
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.SecurePreferences
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.ApiKeyValidator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds QuizRepositoryImpl as the implementation of QuizRepository.
     *
     * @param impl The QuizRepositoryImpl instance
     * @return The QuizRepository instance
     */
    @Binds
    @Singleton
    abstract fun bindQuizRepository(impl: QuizRepositoryImpl): QuizRepository

    /**
     * Binds SettingsRepositoryImpl as the implementation of SettingsRepository.
     *
     * @param impl The SettingsRepositoryImpl instance
     * @return The SettingsRepository instance
     */
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    /**
     * Companion object for providing dependencies that cannot be provided using @Binds
     */
    companion object {
        /**
         * Provides OfflineDataManager instance.
         *
         * @param context The application context
         * @return The OfflineDataManager instance
         */
        @Provides
        @Singleton
        fun provideOfflineDataManager(@ApplicationContext context: Context): OfflineDataManager {
            return OfflineDataManager(context)
        }

        /**
         * Provides OfflineSyncManager instance.
         *
         * @param networkUtils The NetworkUtils instance
         * @param quizRepository The QuizRepository instance
         * @param offlineDataManager The OfflineDataManager instance
         * @return The OfflineSyncManager instance
         */
        @Provides
        @Singleton
        fun provideOfflineSyncManager(
            networkUtils: NetworkUtils,
            quizRepository: QuizRepository,
            offlineDataManager: OfflineDataManager
        ): OfflineSyncManager {
            return OfflineSyncManager(
                networkUtils,
                quizRepository,
                offlineDataManager
            )
        }

        /**
         * Provides the NetworkStateListener instance.
         *
         * @param context The application context
         * @param offlineSyncManager The OfflineSyncManager instance
         * @return The NetworkStateListener instance
         */
        @Provides
        @Singleton
        fun provideNetworkStateListener(
            @ApplicationContext context: Context,
            offlineSyncManager: OfflineSyncManager
        ): NetworkStateListener {
            return NetworkStateListener(context, offlineSyncManager)
        }
    }

    /**
     * Provides the OpenRouterService instance.
     */
    @Provides
    @Singleton
    fun provideOpenRouterService(
        openRouterApi: OpenRouterApi,
        networkUtils: NetworkUtils,
        securePreferences: SecurePreferences,
        apiKeyValidator: ApiKeyValidator
    ): OpenRouterService {
        return OpenRouterService(openRouterApi, networkUtils, securePreferences, apiKeyValidator)
    }

    /**
     * Provides the OpenRouterRepository instance.
     */
    @Provides
    @Singleton
    fun provideOpenRouterRepository(
        openRouterService: OpenRouterService,
        securePreferences: SecurePreferences,
        apiKeyValidator: ApiKeyValidator
    ): OpenRouterRepository {
        return OpenRouterRepository(openRouterService, securePreferences, apiKeyValidator)
    }

    /**
     * Provides the ApiKeyValidator instance.
     */
    @Provides
    @Singleton
    fun provideApiKeyValidator(
        openRouterApi: OpenRouterApi,
        networkUtils: NetworkUtils
    ): ApiKeyValidator {
        return ApiKeyValidator(openRouterApi, networkUtils)
    }
}
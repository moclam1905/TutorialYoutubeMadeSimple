package com.nguyenmoclam.tutorialyoutubemadesimple.di

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepositoryImpl
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.SettingsRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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
}
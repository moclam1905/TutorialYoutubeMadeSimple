package com.nguyenmoclam.tutorialyoutubemadesimple.di

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepository
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.QuizRepositoryImpl
import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.SettingsRepositoryImpl
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module that provides repository bindings.
 * This module is installed in the SingletonComponent to ensure
 * repositories exist for the entire application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds QuizRepositoryImpl as the implementation of QuizRepository.
     *
     * @param impl The QuizRepositoryImpl instance
     * @return The QuizRepository interface
     */
    @Binds
    abstract fun bindQuizRepository(impl: QuizRepositoryImpl): QuizRepository
    
    /**
     * Binds SettingsRepositoryImpl as the implementation of SettingsRepository.
     *
     * @param impl The SettingsRepositoryImpl instance
     * @return The SettingsRepository interface
     */
    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
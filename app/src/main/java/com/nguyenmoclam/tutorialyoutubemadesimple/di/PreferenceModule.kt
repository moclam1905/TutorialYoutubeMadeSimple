package com.nguyenmoclam.tutorialyoutubemadesimple.di

import android.content.Context
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.KeyMigrationManager
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides preferences-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferenceModule {

    /**
     * Provides a singleton instance of SecurePreferences.
     *
     * @param context The application context.
     * @return A singleton instance of SecurePreferences.
     */
    @Provides
    @Singleton
    fun provideSecurePreferences(@ApplicationContext context: Context): SecurePreferences {
        return SecurePreferences(context)
    }

    /**
     * Provides a singleton instance of KeyMigrationManager.
     *
     * @param securePreferences The secure preferences instance.
     * @return A singleton instance of KeyMigrationManager.
     */
    @Provides
    @Singleton
    fun provideKeyMigrationManager(securePreferences: SecurePreferences): KeyMigrationManager {
        return KeyMigrationManager(securePreferences)
    }
} 
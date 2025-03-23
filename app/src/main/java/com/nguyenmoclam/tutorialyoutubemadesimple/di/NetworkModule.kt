package com.nguyenmoclam.tutorialyoutubemadesimple.di

import android.content.Context
import com.nguyenmoclam.tutorialyoutubemadesimple.OpenRouterApi
import com.nguyenmoclam.tutorialyoutubemadesimple.YouTubeApi
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Hilt module that provides network-related dependencies.
 * This module is installed in the SingletonComponent, ensuring
 * that provided dependencies exist for the entire application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides an OkHttpClient instance with logging capabilities.
     * @return Configured OkHttpClient instance
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(networkUtils: NetworkUtils): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return networkUtils.configureOkHttpClient(
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
        )
            .build()
    }

    /**
     * Provides a Retrofit instance configured for YouTube API.
     * @param okHttpClient The OkHttpClient to use for network requests
     * @return Configured Retrofit instance
     */
    @Provides
    @Singleton
    @YouTubeRetrofit
    fun provideYouTubeRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides the YouTube API service interface implementation.
     * @param retrofit The Retrofit instance to create the API service
     * @return Implementation of the YouTube API service
     */
    @Provides
    @Singleton
    fun provideYouTubeApiService(@YouTubeRetrofit retrofit: Retrofit): YouTubeApi {
        return retrofit.create(YouTubeApi::class.java)
    }

    /**
     * Provides a Retrofit instance configured for OpenRouter API.
     * @param okHttpClient The OkHttpClient to use for network requests
     * @return Configured Retrofit instance for OpenRouter
     */
    @Provides
    @Singleton
    @OpenRouterRetrofit
    fun provideOpenRouterRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides the OpenRouter API service interface implementation.
     * @param retrofit The Retrofit instance to create the API service
     * @return Implementation of the OpenRouter API service
     */
    @Provides
    @Singleton
    fun provideOpenRouterApiService(@OpenRouterRetrofit retrofit: Retrofit): OpenRouterApi {
        return retrofit.create(OpenRouterApi::class.java)
    }

    /**
     * Provides a singleton instance of NetworkUtils.
     * This ensures consistent network status and data saver settings across the app.
     *
     * @param context Application context used to access system services
     * @return Singleton instance of NetworkUtils
     */
    @Provides
    @Singleton
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils {
        return NetworkUtils(context)
    }
}
package com.nguyenmoclam.tutorialyoutubemadesimple.di

import javax.inject.Qualifier

/**
 * Qualifier annotation for YouTube API Retrofit instance.
 * Used to distinguish between different Retrofit instances in dependency injection.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class YouTubeRetrofit

/**
 * Qualifier annotation for OpenRouter API Retrofit instance.
 * Used to distinguish between different Retrofit instances in dependency injection.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenRouterRetrofit
package com.nguyenmoclam.tutorialyoutubemadesimple.data.model

/**
 * A simple Result class to handle API responses.
 * Similar to Kotlin's Result class but customized for our needs.
 */
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val error: Throwable) : Result<Nothing>()

    /**
     * Applies the given functions to the encapsulated value if this instance represents [Success]
     * or to the encapsulated error if this instance represents [Failure].
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (Throwable) -> R
    ): R {
        return when (this) {
            is Success -> onSuccess(value)
            is Failure -> onFailure(error)
        }
    }

    /**
     * Returns the encapsulated value if this instance represents [Success] or throws the encapsulated error
     * if it is [Failure].
     */
    fun getOrThrow(): T {
        return when (this) {
            is Success -> value
            is Failure -> throw error
        }
    }
} 
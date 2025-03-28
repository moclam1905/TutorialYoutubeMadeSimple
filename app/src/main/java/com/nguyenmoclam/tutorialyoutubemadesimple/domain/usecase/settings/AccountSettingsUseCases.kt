package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for updating Google sign-in status.
 */
class SetGoogleSignInUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update Google sign-in status.
     *
     * @param signedIn Whether the user is signed in with Google
     */
    suspend operator fun invoke(signedIn: Boolean) {
        settingsRepository.setGoogleSignIn(signedIn)
    }
}

/**
 * Use case for updating transcript mode setting.
 */
class SetTranscriptModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update transcript mode.
     *
     * @param mode The transcript mode ("google" or "anonymous")
     */
    suspend operator fun invoke(mode: String) {
        settingsRepository.setTranscriptMode(mode)
    }
}
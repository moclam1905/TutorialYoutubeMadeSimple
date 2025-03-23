package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.Settings
import com.nguyenmoclam.tutorialyoutubemadesimple.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving current application settings.
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to get current settings.
     *
     * @return Flow of Settings domain model
     */
    operator fun invoke(): Flow<Settings> {
        return settingsRepository.getSettings()
    }
}
package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for updating theme mode setting.
 */
class SetThemeModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update theme mode.
     *
     * @param mode The theme mode to set ("light", "dark", or "system")
     */
    suspend operator fun invoke(mode: String) {
        settingsRepository.setThemeMode(mode)
    }
}

/**
 * Use case for updating app language setting.
 */
class SetAppLanguageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update app language.
     *
     * @param language The language to set ("en", "vi", or "system")
     */
    suspend operator fun invoke(language: String) {
        settingsRepository.setAppLanguage(language)
    }
}
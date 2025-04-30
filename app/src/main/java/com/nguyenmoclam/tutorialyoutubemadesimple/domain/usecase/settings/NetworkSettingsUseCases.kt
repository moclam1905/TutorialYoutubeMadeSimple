package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for updating data saver mode setting.
 */
class SetDataSaverModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update data saver mode.
     *
     * @param enabled Whether data saver mode is enabled
     */
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.setDataSaverMode(enabled)
    }
}

/**
 * Use case for updating connection type setting.
 */
class SetConnectionTypeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update connection type.
     *
     * @param type The connection type ("wifi_only", "mobile_only", or "any")
     */
    suspend operator fun invoke(type: String) {
        settingsRepository.setConnectionType(type)
    }
}

/**
 * Use case for updating connection timeout setting.
 */
class SetConnectionTimeoutUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update connection timeout.
     *
     * @param seconds The timeout in seconds
     */
    suspend operator fun invoke(seconds: Int) {
        settingsRepository.setConnectionTimeout(seconds)
    }
}

/**
 * Use case for updating retry policy setting.
 */
class SetRetryPolicyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update retry policy.
     *
     * @param policy The retry policy ("none", "linear", or "exponential")
     */
    suspend operator fun invoke(policy: String) {
        settingsRepository.setRetryPolicy(policy)
    }
}

/**
 * Use case for updating allow content on metered networks setting.
 */
class SetAllowContentOnMeteredUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update allow content on metered networks setting.
     *
     * @param allowed Whether to allow loading content on metered networks
     */
    suspend operator fun invoke(allowed: Boolean) {
        settingsRepository.setAllowContentOnMetered(allowed)
    }
}
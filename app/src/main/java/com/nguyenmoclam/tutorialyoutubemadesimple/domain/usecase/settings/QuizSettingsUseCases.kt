package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.settings

import com.nguyenmoclam.tutorialyoutubemadesimple.data.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for updating question order setting.
 */
class SetQuestionOrderUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update question order.
     *
     * @param order The question order to set ("sequential" or "shuffle")
     */
    suspend operator fun invoke(order: String) {
        settingsRepository.setQuestionOrder(order)
    }
}

/**
 * Use case for updating max retry count setting.
 */
class SetMaxRetryCountUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update max retry count.
     *
     * @param count The maximum number of retries allowed
     */
    suspend operator fun invoke(count: Int) {
        settingsRepository.setMaxRetryCount(count)
    }
}

/**
 * Use case for updating show answer after wrong setting.
 */
class SetShowAnswerAfterWrongUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update show answer after wrong setting.
     *
     * @param show Whether to show the answer after a wrong attempt
     */
    suspend operator fun invoke(show: Boolean) {
        settingsRepository.setShowAnswerAfterWrong(show)
    }
}

/**
 * Use case for updating auto next question setting.
 */
class SetAutoNextQuestionUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Execute the use case to update auto next question setting.
     *
     * @param auto Whether to automatically proceed to the next question
     */
    suspend operator fun invoke(auto: Boolean) {
        settingsRepository.setAutoNextQuestion(auto)
    }
}
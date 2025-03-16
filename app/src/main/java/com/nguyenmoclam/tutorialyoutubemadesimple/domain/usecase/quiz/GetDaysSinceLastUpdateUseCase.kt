package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Use case for calculating days since last update.
 * This follows the Clean Architecture principle of having use cases represent business logic.
 */
class GetDaysSinceLastUpdateUseCase @Inject constructor() {
    /**
     * Execute the use case to calculate days since last update.
     *
     * @param lastUpdatedTimestamp The timestamp of when the quiz was last updated
     * @return Number of days since the last update
     */
    operator fun invoke(lastUpdatedTimestamp: Long): Int {
        val currentTimeMillis = System.currentTimeMillis()
        val diffMillis = currentTimeMillis - lastUpdatedTimestamp
        return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
    }
}
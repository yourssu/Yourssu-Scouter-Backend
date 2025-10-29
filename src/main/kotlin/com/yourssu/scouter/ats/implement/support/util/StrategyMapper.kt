package com.yourssu.scouter.ats.implement.support.util

import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleStrategy
import com.yourssu.scouter.ats.implement.domain.recruiter.strategy.MaximumDayStrategy
import com.yourssu.scouter.ats.implement.domain.recruiter.strategy.MinimumDayStrategy

object StrategyMapper {

    private val strategyMap = mapOf(
        "MAX" to MaximumDayStrategy(),
        "MIN" to MinimumDayStrategy(),
    )

    fun getStrategy(strategyString: String): ScheduleStrategy {
        return requireNotNull(strategyMap[strategyString]) { "현재 구현되지 않은 전략입니다: $strategyString" }
    }
}
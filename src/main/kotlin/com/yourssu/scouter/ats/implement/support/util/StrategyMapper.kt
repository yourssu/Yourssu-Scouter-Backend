package com.yourssu.scouter.ats.implement.support.util

import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleStrategy
import com.yourssu.scouter.ats.implement.domain.recruiter.strategy.DistributedDayStrategy
import com.yourssu.scouter.ats.implement.domain.recruiter.strategy.ConcentratedDayStrategy

object StrategyMapper {

    private val strategyMap = mapOf(
        "MAX" to DistributedDayStrategy(),
        "MIN" to ConcentratedDayStrategy(),
    )

    fun getStrategy(strategyString: String): ScheduleStrategy {
        return requireNotNull(strategyMap[strategyString]) { "현재 구현되지 않은 전략입니다: $strategyString" }
    }
}
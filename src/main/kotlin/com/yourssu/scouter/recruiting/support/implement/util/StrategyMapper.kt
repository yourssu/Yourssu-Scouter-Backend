package com.yourssu.scouter.recruiting.support.implement.util

import com.yourssu.scouter.recruiting.schedule.implement.ScheduleStrategy
import com.yourssu.scouter.recruiting.schedule.implement.strategy.DistributedDayStrategy
import com.yourssu.scouter.recruiting.schedule.implement.strategy.ConcentratedDayStrategy

object StrategyMapper {

    private val strategyMap = mapOf(
        "MAX" to DistributedDayStrategy(),
        "MIN" to ConcentratedDayStrategy(),
    )

    fun getStrategy(strategyString: String): ScheduleStrategy {
        return requireNotNull(strategyMap[strategyString]) { "현재 구현되지 않은 전략입니다: $strategyString" }
    }
}
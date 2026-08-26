package com.yourssu.scouter.recruiting.schedule.implement.strategy

import com.yourssu.scouter.recruiting.schedule.business.dto.AutoScheduleDto
import com.yourssu.scouter.recruiting.schedule.implement.ScheduleDuplicateKey
import com.yourssu.scouter.recruiting.schedule.implement.ScheduleStrategy
import java.time.ZoneId

class ConcentratedDayStrategy : ScheduleStrategy {

    override fun getPenaltyScore(
        assignedSlot: Set<ScheduleDuplicateKey>,
        schedule: AutoScheduleDto
    ): Long {
        // 현재 배정된 슬롯이 없으면 무조건 새로운 날이므로 1을 반환
        if (assignedSlot.isEmpty()) return 1L

        assignedSlot.forEach { key ->
            // 현재 배정된 슬롯 중 같은 날이 있으면 0을 반환
            val keyDate = key.startTime.atZone(ZoneId.of("Asia/Seoul")).toLocalDate()
            val scheduleDate = schedule.startTime.atZone(ZoneId.of("Asia/Seoul")).toLocalDate()
            if (keyDate.isEqual(scheduleDate)) {
                return 0L
            }
        }

        // 현재 배정된 슬롯 중 같은 날이 없으므로 1을 반환
        return 1L
    }

}
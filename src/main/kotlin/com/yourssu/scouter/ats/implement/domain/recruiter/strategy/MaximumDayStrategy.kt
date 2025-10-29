package com.yourssu.scouter.ats.implement.domain.recruiter.strategy

import com.yourssu.scouter.ats.business.domain.recruiter.AutoScheduleDto
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleDuplicateKey
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleStrategy

class MaximumDayStrategy : ScheduleStrategy {

    override fun getPenaltyScore(
        assignedSlot: Set<ScheduleDuplicateKey>,
        schedule: AutoScheduleDto
    ): Long {
        // 현재 배정된 슬롯이 없으면 무조건 새로운 날이므로 0을 반환
        if (assignedSlot.isEmpty()) return 0L

        assignedSlot.forEach { key ->
            // 현재 배정된 슬롯 중 같은 날이 있으면 1을 반환
            key.interviewTime.dayOfYear == schedule.interviewTime.dayOfYear && return 1L
        }

        // 새로운 날이면 0을 반환
        return 0L
    }
}
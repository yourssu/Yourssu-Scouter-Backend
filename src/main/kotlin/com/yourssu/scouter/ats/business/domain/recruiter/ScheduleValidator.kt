package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.ats.implement.support.exception.DuplicateScheduleException
import org.springframework.stereotype.Component

@Component
class ScheduleValidator {

    fun validateNoDuplicates(schedules: List<Schedule>) {
        val duplicateGroups = schedules
            .groupBy { it.getDuplicateKey() }
            .filter { it.value.size > 1 }

        if (duplicateGroups.isNotEmpty()) {
            val details = duplicateGroups.entries.joinToString("\n") { (key, schedules) ->
                val (partId, time) = key.split("-", limit = 2)
                "파트 $partId, 시간 $time: ${schedules.size}개 중복"
            }
            throw DuplicateScheduleException("중복된 면접 일정이 있습니다:\n$details")
        }
    }
}
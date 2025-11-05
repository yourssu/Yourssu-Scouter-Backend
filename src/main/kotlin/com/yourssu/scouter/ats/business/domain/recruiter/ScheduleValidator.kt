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
                "파트 ${key.partId}, 시간 ${key.startTime}: ${schedules.size}개 중복"
            }
            throw DuplicateScheduleException("중복된 면접 일정이 있습니다:\n$details")
        }

        // 시간 범위 겹침 검증
        validateNoTimeOverlap(schedules)
    }

    /**
     * 같은 파트 내에서 면접 시간이 겹치는지 검증합니다.
     * [startTime, endTime) 범위로 비교합니다.
     */
    private fun validateNoTimeOverlap(schedules: List<Schedule>) {
        val schedulesByPart = schedules.groupBy { it.part.id }

        schedulesByPart.forEach { (partId, partSchedules) ->
            for (i in partSchedules.indices) {
                for (j in i + 1 until partSchedules.size) {
                    val schedule1 = partSchedules[i]
                    val schedule2 = partSchedules[j]

                    // 시간 범위 겹침 체크: [start1, end1)과 [start2, end2)가 겹치는가?
                    val overlaps = schedule1.startTime.isBefore(schedule2.endTime) &&
                                   schedule2.startTime.isBefore(schedule1.endTime)

                    if (overlaps) {
                        throw DuplicateScheduleException(
                            "면접 시간이 겹칩니다: " +
                            "파트 $partId, " +
                            "첫 번째: [${schedule1.startTime} ~ ${schedule1.endTime}), " +
                            "두 번째: [${schedule2.startTime} ~ ${schedule2.endTime})"
                        )
                    }
                }
            }
        }
    }
}
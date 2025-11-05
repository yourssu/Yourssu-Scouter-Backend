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
            val sortedSchedules = partSchedules.sortedBy { it.startTime }

            validateNoTimeOverlapAdjacent(sortedSchedules, partId)
        }
    }

    /**
     * 장렬 되어있는 같은 파트 배열에서 인접한 면접시간을 확인해
     * 겹치는지 검증합니다.
     * [startTime, endTime) 범위로 비교합니다.
     */
    private fun validateNoTimeOverlapAdjacent(
        sortedSchedules: List<Schedule>,
        partId: Long?
    ) {
        for (i in 0 until sortedSchedules.lastIndex) {
            val current = sortedSchedules[i]
            val next = sortedSchedules[i + 1]

            // 정렬 되어 있으므로 인접한 원소만 비교하면 됨 [startTime, endTime) 범위
            if (current.endTime.isAfter(next.startTime)) {
                throw DuplicateScheduleException(
                    "면접 시간이 겹칩니다: " + // 완전히 면접이 겹친게 아닌, 시간이 겹친것이므로 예외메시지를 다르게
                            "파트 $partId, " +
                            "첫 번째: [${current.startTime} ~ ${current.endTime}), " +
                            "두 번째: [${next.startTime} ~ ${next.endTime})"
                )
            }
        }
    }
}
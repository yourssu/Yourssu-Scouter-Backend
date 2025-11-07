package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.ReadScheduleDto
import java.time.LocalDateTime

data class ScheduleDto(
    val id: Long,
    val name: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val part: String,
) {
    companion object {
        fun from(schedule: ReadScheduleDto): ScheduleDto = ScheduleDto(
            id = schedule.id,
            name = schedule.applicantName,
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            part = schedule.part,
        )

    }

}
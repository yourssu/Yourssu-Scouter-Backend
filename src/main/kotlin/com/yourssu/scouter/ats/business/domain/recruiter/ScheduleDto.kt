package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.ReadScheduleDto
import java.time.LocalDateTime

data class ScheduleDto(
    val id: Long,
    val name: String,
    val interviewTime: LocalDateTime,
    val part: String,
) {
    companion object {
        fun from(schedule: ReadScheduleDto): ScheduleDto = ScheduleDto(
            id = schedule.id,
            name = schedule.applicantName,
            interviewTime = schedule.interviewTime,
            part = schedule.part,
        )

    }

}
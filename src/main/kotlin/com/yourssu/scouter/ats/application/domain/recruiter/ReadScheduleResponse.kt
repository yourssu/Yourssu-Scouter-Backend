package com.yourssu.scouter.ats.application.domain.recruiter

import com.yourssu.scouter.ats.business.domain.recruiter.ScheduleDto
import java.time.LocalDateTime

data class ReadScheduleResponse(
    val id: Long,
    val name: String,
    val part: String,
    val time: LocalDateTime,
) {
    companion object {
        fun from(scheduleDto: ScheduleDto) = ReadScheduleResponse(
            id = scheduleDto.id,
            name = scheduleDto.name,
            part = scheduleDto.part,
            time = scheduleDto.interviewTime,
        )
    }
}

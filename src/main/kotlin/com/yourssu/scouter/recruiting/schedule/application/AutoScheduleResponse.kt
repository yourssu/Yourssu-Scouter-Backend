package com.yourssu.scouter.recruiting.schedule.application

import com.yourssu.scouter.recruiting.schedule.business.dto.AutoScheduleDto
import java.time.Instant

class AutoScheduleResponse (
    val applicantId: Long,
    val applicantName: String,
    val part: String,
    val startTime: Instant,
    val endTime: Instant,
) {
    companion object {
        fun from(dto: AutoScheduleDto) = AutoScheduleResponse(
            dto.applicantId,
            dto.applicantName,
            dto.part,
            dto.startTime,
            dto.endTime
        )
    }
}

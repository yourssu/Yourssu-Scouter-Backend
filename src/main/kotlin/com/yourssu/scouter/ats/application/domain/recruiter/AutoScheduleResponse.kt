package com.yourssu.scouter.ats.application.domain.recruiter

import com.yourssu.scouter.ats.business.domain.recruiter.AutoScheduleDto
import java.time.LocalDateTime

class AutoScheduleResponse (
    val applicantId: Long,
    val applicantName: String,
    val part: String,
    val interviewTime: LocalDateTime,
) {
    companion object {
        fun from(dto: AutoScheduleDto) = AutoScheduleResponse(
            dto.applicantId,
            dto.applicantName,
            dto.part,
            dto.interviewTime
        )
    }
}

package com.yourssu.scouter.ats.application.domain.recruiter

import com.yourssu.scouter.ats.business.domain.recruiter.CreateScheduleCommand
import java.time.LocalDateTime

data class CreateScheduleRequest(
    val applicantId: Long,
    val interviewTime: LocalDateTime,
    val partId: Long,
) {
    fun toCommand() = CreateScheduleCommand(
        applicantId = applicantId,
        interviewTime = interviewTime,
        partId = partId,
    )
}
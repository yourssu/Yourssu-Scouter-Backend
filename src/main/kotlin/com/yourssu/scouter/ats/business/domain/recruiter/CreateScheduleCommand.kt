package com.yourssu.scouter.ats.business.domain.recruiter

import java.time.LocalDateTime

data class CreateScheduleCommand (
    val applicantId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val partId: Long,
)
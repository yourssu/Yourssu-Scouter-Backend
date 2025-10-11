package com.yourssu.scouter.ats.business.domain.recruiter

import java.time.LocalDateTime

data class CreateScheduleCommand (
    val applicantId: Long,
    val interviewTime: LocalDateTime,
    val partId: Long,
)
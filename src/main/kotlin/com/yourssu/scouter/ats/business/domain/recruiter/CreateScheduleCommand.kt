package com.yourssu.scouter.ats.business.domain.recruiter

import java.time.Instant

data class CreateScheduleCommand (
    val applicantId: Long,
    val startTime: Instant,
    val endTime: Instant,
    val partId: Long,
)
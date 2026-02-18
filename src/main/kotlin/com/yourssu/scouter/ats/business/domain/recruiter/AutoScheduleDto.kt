package com.yourssu.scouter.ats.business.domain.recruiter

import java.time.Instant

data class AutoScheduleDto (
    val applicantId: Long,
    val applicantName: String,
    val startTime: Instant,
    val endTime: Instant,
    val part: String,
)
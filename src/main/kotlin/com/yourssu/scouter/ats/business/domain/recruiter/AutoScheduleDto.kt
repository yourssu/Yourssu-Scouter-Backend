package com.yourssu.scouter.ats.business.domain.recruiter

import java.time.LocalDateTime

data class AutoScheduleDto (
    val applicantId: Long,
    val applicantName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val part: String,
)
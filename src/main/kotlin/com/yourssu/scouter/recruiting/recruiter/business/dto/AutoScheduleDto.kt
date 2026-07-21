package com.yourssu.scouter.recruiting.recruiter.business.dto

import java.time.Instant

data class AutoScheduleDto (
    val applicantId: Long,
    val applicantName: String,
    val startTime: Instant,
    val endTime: Instant,
    val part: String,
)
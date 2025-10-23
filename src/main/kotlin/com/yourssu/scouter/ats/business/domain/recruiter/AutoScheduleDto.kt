package com.yourssu.scouter.ats.business.domain.recruiter

import java.time.LocalDateTime

data class AutoScheduleDto (
    val applicantId: Long,
    val applicantName: String,
    val interviewTime: LocalDateTime,
    val part: String,
)
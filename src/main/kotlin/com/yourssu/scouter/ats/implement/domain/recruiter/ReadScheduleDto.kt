package com.yourssu.scouter.ats.implement.domain.recruiter

import java.time.LocalDateTime

data class ReadScheduleDto(
    val id: Long,
    val applicantId: Long,
    val applicantName: String,
    val part: String,
    val interviewTime: LocalDateTime,
)
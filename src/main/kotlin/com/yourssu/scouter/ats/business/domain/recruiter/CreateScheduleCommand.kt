package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleLocationType
import java.time.Instant

data class CreateScheduleCommand(
    val applicantId: Long,
    val startTime: Instant,
    val endTime: Instant,
    val partId: Long,
    val locationType: ScheduleLocationType,
    val locationDetail: String? = null,
)

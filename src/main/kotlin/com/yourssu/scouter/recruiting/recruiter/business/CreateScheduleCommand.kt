package com.yourssu.scouter.recruiting.recruiter.business

import com.yourssu.scouter.recruiting.recruiter.implement.ScheduleLocationType
import java.time.Instant

data class CreateScheduleCommand(
    val applicantId: Long,
    val startTime: Instant,
    val endTime: Instant,
    val partId: Long,
    val locationType: ScheduleLocationType,
    val locationDetail: String? = null,
)

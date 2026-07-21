package com.yourssu.scouter.recruiting.schedule.business

import com.yourssu.scouter.recruiting.schedule.implement.ScheduleLocationType
import java.time.Instant

data class CreateScheduleCommand(
    val applicantId: Long,
    val startTime: Instant,
    val endTime: Instant,
    val partId: Long,
    val locationType: ScheduleLocationType,
    val locationDetail: String? = null,
)

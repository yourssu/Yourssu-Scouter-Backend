package com.yourssu.scouter.ats.implement.domain.recruiter

import java.time.Instant

data class ReadScheduleDto(
    val id: Long,
    val applicantId: Long,
    val applicantName: String,
    val part: String,
    val startTime: Instant,
    val endTime: Instant,
    val locationType: ScheduleLocationType = ScheduleLocationType.CLUB_ROOM,
    val locationDetail: String? = null,
)

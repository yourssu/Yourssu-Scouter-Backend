package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleLocationType

data class UpdateScheduleLocationCommand(
    val scheduleId: Long,
    val locationType: ScheduleLocationType,
    val locationDetail: String?,
)

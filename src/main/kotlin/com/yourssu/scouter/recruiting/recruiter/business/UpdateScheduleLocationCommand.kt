package com.yourssu.scouter.recruiting.recruiter.business

import com.yourssu.scouter.recruiting.recruiter.implement.ScheduleLocationType

data class UpdateScheduleLocationCommand(
    val scheduleId: Long,
    val locationType: ScheduleLocationType,
    val locationDetail: String?,
)

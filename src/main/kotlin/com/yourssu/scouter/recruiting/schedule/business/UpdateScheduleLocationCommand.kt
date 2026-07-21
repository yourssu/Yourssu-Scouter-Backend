package com.yourssu.scouter.recruiting.schedule.business

import com.yourssu.scouter.recruiting.schedule.implement.ScheduleLocationType

data class UpdateScheduleLocationCommand(
    val scheduleId: Long,
    val locationType: ScheduleLocationType,
    val locationDetail: String?,
)

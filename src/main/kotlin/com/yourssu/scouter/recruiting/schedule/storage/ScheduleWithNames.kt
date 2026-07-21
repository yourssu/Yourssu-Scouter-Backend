package com.yourssu.scouter.recruiting.schedule.storage

import com.yourssu.scouter.recruiting.schedule.implement.ReadScheduleDto
import com.yourssu.scouter.recruiting.schedule.implement.ScheduleLocationType
import java.time.Instant

data class ScheduleWithNames(
    val id: Long,
    val applicantId: Long,
    val applicantName: String,
    val partName: String,
    val startTime: Instant,
    val endTime: Instant,
    val locationType: ScheduleLocationType,
    val locationDetail: String?,
) {
    fun toDomain() =
        ReadScheduleDto(
            id = id,
            applicantId = applicantId,
            applicantName = applicantName,
            part = partName,
            startTime = startTime,
            endTime = endTime,
            locationType = locationType,
            locationDetail = locationDetail,
        )
}

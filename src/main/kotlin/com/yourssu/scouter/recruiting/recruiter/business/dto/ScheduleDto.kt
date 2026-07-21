package com.yourssu.scouter.recruiting.recruiter.business.dto

import com.yourssu.scouter.recruiting.support.business.utils.ScheduleLocationTypeConverter
import com.yourssu.scouter.recruiting.recruiter.implement.ReadScheduleDto
import java.time.Instant

data class ScheduleDto(
    val id: Long,
    val applicantId: Long,
    val name: String,
    val startTime: Instant,
    val endTime: Instant,
    val part: String,
    val locationType: String,
    val locationDetail: String?,
) {
    companion object {
        fun from(schedule: ReadScheduleDto): ScheduleDto =
            ScheduleDto(
                id = schedule.id,
                applicantId = schedule.applicantId,
                name = schedule.applicantName,
                startTime = schedule.startTime,
                endTime = schedule.endTime,
                part = schedule.part,
                locationType = ScheduleLocationTypeConverter.convertToString(schedule.locationType),
                locationDetail = schedule.locationDetail,
            )
    }
}

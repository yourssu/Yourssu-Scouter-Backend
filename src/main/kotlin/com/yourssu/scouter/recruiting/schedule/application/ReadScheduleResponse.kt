package com.yourssu.scouter.recruiting.schedule.application

import com.yourssu.scouter.recruiting.schedule.business.dto.ScheduleDto
import java.time.Instant

data class ReadScheduleResponse(
    val id: Long,
    val applicantId: Long,
    val name: String,
    val part: String,
    val startTime: Instant,
    val endTime: Instant,
    val locationType: String,
    val locationDetail: String?,
) {
    companion object {
        fun from(scheduleDto: ScheduleDto) =
            ReadScheduleResponse(
                id = scheduleDto.id,
                applicantId = scheduleDto.applicantId,
                name = scheduleDto.name,
                part = scheduleDto.part,
                startTime = scheduleDto.startTime,
                endTime = scheduleDto.endTime,
                locationType = scheduleDto.locationType,
                locationDetail = scheduleDto.locationDetail,
            )
    }
}

package com.yourssu.scouter.ats.storage.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.ReadScheduleDto
import java.time.Instant

data class ScheduleWithNames(
    val id: Long,
    val applicantId: Long,
    val applicantName: String,
    val partName: String,
    val startTime: Instant,
    val endTime: Instant,
) {
    fun toDomain() = ReadScheduleDto(
        id = id,
        applicantId = applicantId,
        applicantName = applicantName,
        part = partName,
        startTime = startTime,
        endTime = endTime,
    )
}

package com.yourssu.scouter.ats.storage.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.ReadScheduleDto
import java.time.LocalDateTime

data class ScheduleWithNames(
    val id: Long,
    val applicantName: String,
    val partName: String,
    val interviewTime: LocalDateTime,
) {
    fun toDomain() = ReadScheduleDto(
        id = id,
        name = applicantName,
        interviewTime = interviewTime,
        part = partName,
    )
}

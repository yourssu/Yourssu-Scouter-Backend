package com.yourssu.scouter.ats.application.domain.recruiter

import com.yourssu.scouter.ats.business.domain.recruiter.CreateScheduleCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class CreateScheduleRequest(
    @field:NotNull(message = "지원자 ID를 입력하지 않았습니다.")
    val applicantId: Long,

    @field:NotNull(message = "면접 시간을 입력하지 않았습니다.")
    @field:Schema(pattern = "yyyy-MM-ddTHH:mm:ssZ", example = "2025-11-10T10:00:00Z")
    val startTime: Instant,

    @field:NotNull(message = "면접 시간을 입력하지 않았습니다.")
    @field:Schema(pattern = "yyyy-MM-ddTHH:mm:ssZ", example = "2025-11-10T10:30:00Z")
    val endTime: Instant,

    @field:NotNull(message = "파트 ID를 입력하지 않았습니다.")
    val partId: Long,
) {
    fun toCommand() = CreateScheduleCommand(
        applicantId = applicantId,
        startTime = startTime,
        endTime = endTime,
        partId = partId,
    )
}
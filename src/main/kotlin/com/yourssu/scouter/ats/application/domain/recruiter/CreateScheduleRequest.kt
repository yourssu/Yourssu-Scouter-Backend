package com.yourssu.scouter.ats.application.domain.recruiter

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.ats.business.domain.recruiter.CreateScheduleCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class CreateScheduleRequest(
    @field:NotNull(message = "지원자 ID를 입력하지 않았습니다.")
    var applicantId: Long,

    @field:Future
    @field:NotNull(message = "면접 시간을 입력하지 않았습니다.")
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @field:Schema(pattern = "yyyy-MM-ddTHH:mm", type = "string", format = "date-time", example = "2025-11-10T10:00")
    var interviewTime: LocalDateTime,

    @field:NotNull(message = "파트 ID를 입력하지 않았습니다.")
    var partId: Long,
) {
    fun toCommand() = CreateScheduleCommand(
        applicantId = applicantId,
        interviewTime = interviewTime,
        partId = partId,
    )
}
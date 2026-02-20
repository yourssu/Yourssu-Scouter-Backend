package com.yourssu.scouter.ats.application.domain.recruiter

import com.yourssu.scouter.ats.business.domain.recruiter.CreateScheduleCommand
import com.yourssu.scouter.ats.business.support.utils.ScheduleLocationTypeConverter
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
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
    @field:NotBlank(message = "면접 장소 대분류를 입력하지 않았습니다.")
    @field:Schema(description = "동방 | 강의실 | 비대면 | 기타", example = "동방")
    val locationType: String,
    val locationDetail: String? = null,
) {
    fun toCommand() =
        CreateScheduleCommand(
            applicantId = applicantId,
            startTime = startTime,
            endTime = endTime,
            partId = partId,
            locationType = ScheduleLocationTypeConverter.convertToEnum(locationType),
            locationDetail = locationDetail,
        )
}

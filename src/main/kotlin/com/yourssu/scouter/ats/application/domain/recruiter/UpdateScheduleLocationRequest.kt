package com.yourssu.scouter.ats.application.domain.recruiter

import com.yourssu.scouter.ats.business.domain.recruiter.UpdateScheduleLocationCommand
import com.yourssu.scouter.ats.business.support.utils.ScheduleLocationTypeConverter
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class UpdateScheduleLocationRequest(
    @field:NotBlank(message = "면접 장소 대분류를 입력하지 않았습니다.")
    @field:Schema(description = "동방 | 강의실 | 비대면 | 기타", example = "동방")
    val locationType: String,
    val locationDetail: String? = null,
) {
    fun toCommand(scheduleId: Long): UpdateScheduleLocationCommand =
        UpdateScheduleLocationCommand(
            scheduleId = scheduleId,
            locationType = ScheduleLocationTypeConverter.convertToEnum(locationType),
            locationDetail = locationDetail,
        )
}

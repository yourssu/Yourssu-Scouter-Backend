package com.yourssu.scouter.recruiting.rubric.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.Instant

@Schema(description = "면접 평가 루브릭 수정 마감일 변경 요청")
data class InterviewRubricDeadlineUpdateRequest(
    @field:NotNull
    @field:Schema(description = "루브릭 수정 마감 시각(UTC ISO-8601)", example = "2026-08-01T09:00:00Z", type = "string", format = "date-time", nullable = false)
    val deadline: Instant?,
)

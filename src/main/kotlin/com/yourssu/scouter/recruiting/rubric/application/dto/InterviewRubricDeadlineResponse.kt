package com.yourssu.scouter.recruiting.rubric.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class InterviewRubricDeadlineResponse(
    @field:Schema(description = "루브릭 수정 마감 시각(UTC ISO-8601)", example = "2026-08-01T09:00:00Z", type = "string", format = "date-time")
    val deadline: Instant,
)

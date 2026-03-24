package com.yourssu.scouter.hrms.application.domain.member

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class LastMemberSyncTimeResponse(
    @field:Schema(description = "마지막 동기화 시각(UTC, 없으면 null)", example = "2026-03-24T09:30:00Z")
    val lastUpdatedTime: Instant?,
)

package com.yourssu.scouter.recruiting.evaluation.application.dto

import com.yourssu.scouter.recruiting.evaluation.business.dto.EvaluatorStatusDto
import com.yourssu.scouter.recruiting.evaluation.implement.EvaluationStatus
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "지원자에 대한 면접 평가자별 작성 진행 상태")
data class ReadEvaluatorStatusResponse(
    @field:Schema(description = "평가자 사용자 ID (User 엔티티가 없는 경우 null)", example = "23", nullable = true, types=["integer", "null"])
    val userId: Long?,
    @field:Schema(description = "평가자 이름", example = "홍길동", nullable = false)
    val name: String,
    @field:Schema(description = "평가 작성 상태", example = "SUBMITTED", allowableValues = ["NOT_STARTED", "IN_PROGRESS", "SUBMITTED"], nullable = false)
    val status: EvaluationStatus,
) {
    companion object {
        fun from(dto: EvaluatorStatusDto): ReadEvaluatorStatusResponse =
            ReadEvaluatorStatusResponse(dto.userId, dto.name, dto.status)
    }
}

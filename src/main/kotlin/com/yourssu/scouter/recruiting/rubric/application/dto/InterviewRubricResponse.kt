package com.yourssu.scouter.recruiting.rubric.application.dto

import com.yourssu.scouter.recruiting.rubric.business.dto.InterviewRubricResult
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import java.time.Instant
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "면접 루브릭 조회 또는 저장 응답")
data class InterviewRubricResponse(
    @field:Schema(description = "루브릭 ID", example = "1")
    val id: Long,
    @field:Schema(description = "파트 ID", example = "3")
    val partId: Long,
    @field:Schema(description = "학기 식별자", example = "2026-1")
    val semester: String,
    @field:Schema(description = "루브릭 수정 마감 시각(UTC ISO-8601)", example = "2026-08-01T09:00:00Z", type = "string", format = "date-time")
    val deadline: Instant,
    @field:Schema(description = "잠금 여부. true이면 수정 불가", example = "false")
    val isLocked: Boolean,
    @field:Schema(description = "지원자 한 명을 평가할 면접관 수", example = "2")
    val interviewerCount: Int,
    @field:Schema(description = "평가 항목 목록")
    val items: List<ItemResponse>
) {
    @Schema(description = "면접 루브릭 평가 항목 응답")
    data class ItemResponse(
        @field:Schema(description = "평가 항목 ID", example = "10")
        val id: Long,
        @field:Schema(description = "평가 항목명", example = "직무 역량")
        val keyword: String,
        @field:Schema(description = "평가 그룹", example = "JOB", allowableValues = ["JOB", "CULTURE", "TEAM"])
        val group: RubricGroupType,
        @field:Schema(description = "항목 최대 배점", example = "60")
        val maxScore: Int
    )

    companion object {
        fun from(result: InterviewRubricResult): InterviewRubricResponse {
            return InterviewRubricResponse(
                id = result.id,
                partId = result.partId,
                semester = result.semester,
                deadline = result.deadline,
                isLocked = result.isLocked,
                interviewerCount = result.interviewerCount,
                items = result.items.map {
                    ItemResponse(
                        id = it.id,
                        keyword = it.keyword,
                        group = it.rubricType,
                        maxScore = it.maxScore
                    )
                }
            )
        }
    }
}

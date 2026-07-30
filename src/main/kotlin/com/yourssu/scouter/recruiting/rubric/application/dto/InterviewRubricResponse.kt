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
    @field:Schema(description = "평가 항목 그룹 목록")
    val groups: List<GroupResponse>
) {
    @Schema(description = "면접 루브릭 평가 항목 그룹 응답")
    data class GroupResponse(
        @field:Schema(description = "평가 그룹", example = "CULTURE_FIT", allowableValues = ["JOB_FIT", "CULTURE_FIT", "TEAM_FIT"])
        val group: String,
        @field:Schema(description = "그룹 합계 배점", example = "30")
        val groupMaxScore: Int,
        @field:Schema(description = "평가 항목 목록")
        val items: List<ItemResponse>
    )

    @Schema(description = "면접 루브릭 평가 항목 응답")
    data class ItemResponse(
        @field:Schema(description = "평가 항목 ID", example = "1")
        val itemId: Long,
        @field:Schema(description = "평가 항목명", example = "주도성, 실행력")
        val title: String,
        @field:Schema(description = "항목 최대 배점", example = "10")
        val maxScore: Int
    )

    companion object {
        fun from(result: InterviewRubricResult): InterviewRubricResponse {
            val itemsByGroup = result.items.groupBy { it.rubricType }
            val groups = listOf(RubricGroupType.CULTURE, RubricGroupType.TEAM, RubricGroupType.JOB).map { groupType ->
                val items = itemsByGroup[groupType] ?: emptyList()
                GroupResponse(
                    group = when(groupType) {
                        RubricGroupType.CULTURE -> "CULTURE_FIT"
                        RubricGroupType.TEAM -> "TEAM_FIT"
                        RubricGroupType.JOB -> "JOB_FIT"
                        RubricGroupType.OTHER -> "OTHER"
                    },
                    groupMaxScore = items.sumOf { it.maxScore },
                    items = items.map {
                        ItemResponse(
                            itemId = it.id ?: 0L,
                            title = it.keyword,
                            maxScore = it.maxScore
                        )
                    }
                )
            }

            return InterviewRubricResponse(
                id = result.id,
                partId = result.partId,
                semester = result.semester,
                deadline = result.deadline,
                isLocked = result.isLocked,
                groups = groups
            )
        }
    }
}

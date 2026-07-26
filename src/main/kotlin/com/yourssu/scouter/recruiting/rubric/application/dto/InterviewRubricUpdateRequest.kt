package com.yourssu.scouter.recruiting.rubric.application.dto

import com.yourssu.scouter.recruiting.rubric.business.dto.UpdateInterviewRubricCommand
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import java.time.Instant
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@Schema(description = "면접 루브릭 등록 또는 수정 요청")
data class InterviewRubricUpdateRequest(
    @field:NotNull
    @field:Schema(description = "루브릭 수정 마감 시각(UTC ISO-8601)", example = "2026-08-01T09:00:00Z", type = "string", format = "date-time")
    val deadline: Instant,

    @field:Min(1)
    @field:Schema(description = "지원자 한 명을 평가할 면접관 수. 1 이상", example = "2")
    val interviewerCount: Int,

    @field:NotEmpty
    @field:Valid
    @field:Schema(description = "평가 항목 그룹 목록")
    val groups: List<GroupRequest>
) {
    @Schema(description = "면접 루브릭 평가 항목 그룹 요청")
    data class GroupRequest(
        @field:NotBlank
        @field:Schema(description = "평가 그룹", example = "CULTURE_FIT", allowableValues = ["JOB_FIT", "CULTURE_FIT", "TEAM_FIT"])
        val group: String,

        @field:NotEmpty
        @field:Valid
        @field:Schema(description = "평가 항목 목록")
        val items: List<ItemRequest>
    )

    @Schema(description = "면접 루브릭 평가 항목 요청")
    data class ItemRequest(
        @field:Schema(description = "기존 항목 수정 시 해당 항목 ID. 새 항목이면 생략", example = "10", nullable = true)
        val itemId: Long? = null,

        @field:NotBlank
        @field:Schema(description = "평가 항목명", example = "주도성, 실행력")
        val title: String,

        @field:Min(0)
        @field:Schema(description = "항목의 최대 배점. 0 이상", example = "10")
        val maxScore: Int
    )

    fun toCommand(partId: Long, semester: String): UpdateInterviewRubricCommand {
        val commandItems = groups.flatMap { groupReq ->
            val rubricType = when(groupReq.group) {
                "CULTURE_FIT" -> RubricGroupType.CULTURE
                "TEAM_FIT" -> RubricGroupType.TEAM
                "JOB_FIT" -> RubricGroupType.JOB
                else -> throw IllegalArgumentException("유효하지 않은 평가 그룹입니다: ${groupReq.group}")
            }
            groupReq.items.map { itemReq ->
                UpdateInterviewRubricCommand.ItemCommand(
                    id = itemReq.itemId,
                    keyword = itemReq.title,
                    rubricType = rubricType,
                    maxScore = itemReq.maxScore
                )
            }
        }
        return UpdateInterviewRubricCommand(
            partId = partId,
            semester = semester,
            deadline = deadline,
            interviewerCount = interviewerCount,
            items = commandItems
        )
    }
}

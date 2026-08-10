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
        @field:NotNull
        @field:Schema(description = "항목 ID", example = "10")
        val itemId: Long,

        @field:Min(0)
        @field:Schema(description = "항목의 최대 배점. 0 이상", example = "10")
        val maxScore: Int
    )

    fun toCommand(partId: Long, semester: String): UpdateInterviewRubricCommand {
        val commandItems = groups.flatMap { groupReq ->
            groupReq.items.map { itemReq ->
                UpdateInterviewRubricCommand.ItemCommand(
                    id = itemReq.itemId,
                    maxScore = itemReq.maxScore
                )
            }
        }
        return UpdateInterviewRubricCommand(
            partId = partId,
            semester = semester,
            deadline = deadline,
            items = commandItems
        )
    }
}

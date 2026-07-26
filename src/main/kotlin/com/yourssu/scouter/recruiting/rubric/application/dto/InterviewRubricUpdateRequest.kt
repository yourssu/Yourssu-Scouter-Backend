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
    @field:Schema(description = "평가 항목 목록. 항목의 maxScore 합계는 반드시 100")
    val items: List<ItemRequest>
) {
    @Schema(description = "면접 루브릭 평가 항목")
    data class ItemRequest(
        @field:Schema(description = "기존 항목 수정 시 해당 항목 ID. 새 항목이면 생략", example = "10", nullable = true)
        val id: Long? = null,

        @field:NotBlank
        @field:Schema(description = "평가 항목명", example = "직무 역량")
        val keyword: String,

        @field:NotNull
        @field:Schema(description = "평가 그룹", example = "JOB", allowableValues = ["JOB", "CULTURE", "TEAM"])
        val group: RubricGroupType,

        @field:Min(0)
        @field:Schema(description = "항목의 최대 배점. 0 이상이며 전체 항목의 합계는 100", example = "60")
        val maxScore: Int
    )

    fun toCommand(partId: Long, semester: String): UpdateInterviewRubricCommand {
        return UpdateInterviewRubricCommand(
            partId = partId,
            semester = semester,
            deadline = deadline,
            interviewerCount = interviewerCount,
            items = items.map {
                UpdateInterviewRubricCommand.ItemCommand(
                    id = it.id,
                    keyword = it.keyword,
                    rubricType = it.group,
                    maxScore = it.maxScore
                )
            }
        )
    }
}

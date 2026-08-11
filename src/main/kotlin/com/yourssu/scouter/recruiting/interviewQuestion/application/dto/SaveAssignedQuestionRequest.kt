package com.yourssu.scouter.recruiting.interviewQuestion.application.dto

import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionCommand
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

data class SaveAssignedQuestionRequest(

    @field:Schema(description = "배정할 면접관 유저 ID")
    val assignedInterviewerUserId: Long?,

    @field:Schema(description = "카탈로그 원본 질문 ID (개인 질문인 경우 null)", nullable = true)
    val sourceQuestionId: Long?,

    @field:Schema(description = "질문 내용 (개인 질문이거나 PART 질문의 내용을 수정할 경우)", nullable = true)
    val content: String?,

    @field:NotNull
    @field:Schema(description = "질문 카테고리")
    val category: AssignedQuestionCategory?,

    @field:Schema(description = "질문 선택 여부", nullable = true)
    val isSelected: Boolean? = null,

    @field:Schema(description = "질문 요구조건 ID 목록 (PERSONAL 혹은 PART 질문 수정 시 필요)")
    val requirementIds: List<Long> = emptyList(),
) {
    fun toCommand(): SaveAssignedQuestionCommand = SaveAssignedQuestionCommand(
        assignedInterviewerUserId = assignedInterviewerUserId!!,
        sourceQuestionId = sourceQuestionId,
        content = content,
        category = category!!,
        isSelected = isSelected,
        requirementIds = requirementIds,
    )
}

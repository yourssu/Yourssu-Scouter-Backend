package com.yourssu.scouter.recruiting.interviewQuestion.application.dto

import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.AssignedQuestionDto
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import io.swagger.v3.oas.annotations.media.Schema

data class ReadAssignedQuestionResponse(
    @field:Schema(description = "배정 질문 ID (초기 기본 질문인 경우 null)", nullable = true)
    val id: Long?,

    @field:Schema(description = "Assigned interviewer user ID (null for initial default questions)", nullable = true)
    val assignedInterviewerUserId: Long?,

    @field:Schema(description = "배정된 면접관 영어 닉네임", nullable = true)
    val assignedInterviewerName: String?,

    @field:Schema(description = "카탈로그 원본 질문 ID (개인 질문인 경우 null)", nullable = true)
    val sourceQuestionId: Long?,

    @field:Schema(description = "질문 내용")
    val content: String,

    @field:Schema(description = "질문 카테고리")
    val category: AssignedQuestionCategory,

    @field:Schema(description = "질문 선택 여부", nullable = true)
    val isSelected: Boolean?,

    @field:Schema(description = "질문 요구조건 리스트")
    val requirements: List<ReadQuestionRequirementResponse>,
) {
    companion object {
        fun from(dto: AssignedQuestionDto): ReadAssignedQuestionResponse = ReadAssignedQuestionResponse(
            id = dto.id,
            assignedInterviewerUserId = dto.assignedInterviewerUserId,
            assignedInterviewerName = dto.assignedInterviewerName,
            sourceQuestionId = dto.sourceQuestionId,
            content = dto.content,
            category = dto.category,
            isSelected = dto.isSelected,
            requirements = dto.requirements.map { ReadQuestionRequirementResponse.from(it) },
        )
    }
}

package com.yourssu.scouter.recruiting.interviewQuestion.application.dto

import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.AssignedQuestionDto
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory

data class ReadAssignedQuestionResponse(
    val id: Long?,
    val assignedInterviewerUserId: Long?,
    val sourceQuestionId: Long?,
    val content: String,
    val category: AssignedQuestionCategory,
    val isSelected: Boolean?,
    val requirements: List<ReadQuestionRequirementResponse>,
) {
    companion object {
        fun from(dto: AssignedQuestionDto): ReadAssignedQuestionResponse = ReadAssignedQuestionResponse(
            id = dto.id,
            assignedInterviewerUserId = dto.assignedInterviewerUserId,
            sourceQuestionId = dto.sourceQuestionId,
            content = dto.content,
            category = dto.category,
            isSelected = dto.isSelected,
            requirements = dto.requirements.map { ReadQuestionRequirementResponse.from(it) },
        )
    }
}

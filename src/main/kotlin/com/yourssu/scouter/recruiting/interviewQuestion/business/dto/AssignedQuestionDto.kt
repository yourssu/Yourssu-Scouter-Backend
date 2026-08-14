package com.yourssu.scouter.recruiting.interviewQuestion.business.dto

import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory

data class AssignedQuestionDto(
    val id: Long?,
    val assignedInterviewerName: String?,
    val sourceQuestionId: Long?,
    val content: String,
    val category: AssignedQuestionCategory,
    val sortOrder: Int,
    val isSelected: Boolean? = null,
    val requirements: List<QuestionRequirementDto> = emptyList(),
)

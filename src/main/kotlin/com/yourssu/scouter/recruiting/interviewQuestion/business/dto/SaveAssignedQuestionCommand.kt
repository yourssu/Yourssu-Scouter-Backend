package com.yourssu.scouter.recruiting.interviewQuestion.business.dto

import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory

data class SaveAssignedQuestionCommand(
    val assignedInterviewerUserId: Long,
    val sourceQuestionId: Long?,
    val content: String?,
    val category: AssignedQuestionCategory,
    val isSelected: Boolean? = null,
    val requirementIds: List<Long> = emptyList(),
)

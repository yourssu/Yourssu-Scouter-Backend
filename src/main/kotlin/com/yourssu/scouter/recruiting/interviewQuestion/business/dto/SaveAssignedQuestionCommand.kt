package com.yourssu.scouter.recruiting.interviewQuestion.business.dto

import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionDraft

data class SaveAssignedQuestionCommand(
    val assignedMemberId: Long,
    val sourceQuestionId: Long?,
    val content: String?,
    val category: AssignedQuestionCategory,
    val isSelected: Boolean? = null,
    val requirementIds: List<Long>? = null,
)

fun SaveAssignedQuestionCommand.toDraft(): AssignedQuestionDraft =
    AssignedQuestionDraft(
        assignedMemberId = assignedMemberId,
        sourceQuestionId = sourceQuestionId,
        content = content,
        category = category,
        isSelected = isSelected,
        requirementIds = requirementIds,
    )

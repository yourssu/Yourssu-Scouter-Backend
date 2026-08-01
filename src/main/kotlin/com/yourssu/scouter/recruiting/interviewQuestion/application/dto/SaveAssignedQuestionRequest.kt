package com.yourssu.scouter.recruiting.interviewQuestion.application.dto

import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionCommand
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import jakarta.validation.constraints.NotNull

data class SaveAssignedQuestionRequest(

    @field:NotNull
    val assignedInterviewerUserId: Long?,

    val sourceQuestionId: Long?,

    val content: String?,

    @field:NotNull
    val category: AssignedQuestionCategory?,

    val isSelected: Boolean? = null,

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

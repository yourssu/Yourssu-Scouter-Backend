package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.interview.business.dto.SaveInterviewMemoCommand
import jakarta.validation.constraints.NotNull

data class SaveInterviewMemoRequest(

    @field:NotNull
    val assignedQuestionId: Long?,

    @field:NotNull
    val memo: String?,
) {
    fun toCommand(): SaveInterviewMemoCommand = SaveInterviewMemoCommand(
        assignedQuestionId = assignedQuestionId!!,
        memo = memo!!,
    )
}

package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.interview.business.dto.SaveInterviewMemoCommand
import jakarta.validation.constraints.NotNull

data class SaveInterviewMemoRequest(

    @field:NotNull
    val questionnaireQuestionId: Long?,

    @field:NotNull
    val memo: String?,
) {
    fun toCommand(): SaveInterviewMemoCommand = SaveInterviewMemoCommand(
        questionnaireQuestionId = questionnaireQuestionId!!,
        memo = memo!!,
    )
}

package com.yourssu.scouter.recruiting.question.application.dto

import com.yourssu.scouter.recruiting.question.business.dto.SaveQuestionnaireQuestionCommand
import com.yourssu.scouter.recruiting.question.implement.QuestionGroup
import jakarta.validation.constraints.NotNull

data class SaveQuestionnaireQuestionRequest(

    @field:NotNull
    val group: QuestionGroup?,

    val sourceQuestionId: Long?,

    val content: String?,
) {
    fun toCommand(): SaveQuestionnaireQuestionCommand = SaveQuestionnaireQuestionCommand(
        group = group!!,
        sourceQuestionId = sourceQuestionId,
        content = content,
    )
}

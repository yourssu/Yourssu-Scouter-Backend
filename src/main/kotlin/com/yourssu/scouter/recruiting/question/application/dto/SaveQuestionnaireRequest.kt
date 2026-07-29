package com.yourssu.scouter.recruiting.question.application.dto

import com.yourssu.scouter.recruiting.question.business.dto.SaveQuestionnaireCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class SaveQuestionnaireRequest(

    @field:NotNull
    val assignedInterviewerUserId: Long?,

    @field:Valid
    val questions: List<SaveQuestionnaireQuestionRequest>,
) {
    fun toCommand(): SaveQuestionnaireCommand = SaveQuestionnaireCommand(
        assignedInterviewerUserId = assignedInterviewerUserId!!,
        questions = questions.map { it.toCommand() },
    )
}

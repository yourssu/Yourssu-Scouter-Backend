package com.yourssu.scouter.recruiting.interviewQuestion.application.dto

import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionsCommand
import jakarta.validation.Valid

data class SaveAssignedQuestionsRequest(
    @field:Valid
    val questions: List<SaveAssignedQuestionRequest>,
) {
    fun toCommand(): SaveAssignedQuestionsCommand = SaveAssignedQuestionsCommand(
        questions = questions.map { it.toCommand() },
    )
}

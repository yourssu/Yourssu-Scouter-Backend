package com.yourssu.scouter.recruiting.question.application.dto

import com.yourssu.scouter.recruiting.question.business.dto.QuestionnaireDto

data class ReadQuestionnaireResponse(
    val assignedInterviewerUserId: Long?,
    val questions: List<ReadQuestionnaireQuestionResponse>,
) {
    companion object {
        fun from(dto: QuestionnaireDto): ReadQuestionnaireResponse = ReadQuestionnaireResponse(
            assignedInterviewerUserId = dto.assignedInterviewerUserId,
            questions = dto.questions.map(ReadQuestionnaireQuestionResponse::from),
        )
    }
}

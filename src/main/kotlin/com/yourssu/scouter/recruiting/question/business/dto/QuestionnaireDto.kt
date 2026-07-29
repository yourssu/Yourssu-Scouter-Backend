package com.yourssu.scouter.recruiting.question.business.dto

data class QuestionnaireDto(
    val applicantId: Long,
    val assignedInterviewerUserId: Long?,
    val questions: List<QuestionnaireQuestionDto>,
)

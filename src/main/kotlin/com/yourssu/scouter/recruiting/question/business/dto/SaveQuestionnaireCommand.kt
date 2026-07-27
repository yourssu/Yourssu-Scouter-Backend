package com.yourssu.scouter.recruiting.question.business.dto

data class SaveQuestionnaireCommand(
    val assignedInterviewerUserId: Long,
    val questions: List<SaveQuestionnaireQuestionCommand>,
)

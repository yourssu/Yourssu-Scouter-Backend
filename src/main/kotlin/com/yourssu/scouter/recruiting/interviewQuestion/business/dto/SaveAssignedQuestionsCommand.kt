package com.yourssu.scouter.recruiting.interviewQuestion.business.dto

data class SaveAssignedQuestionsCommand(
    val questions: List<SaveAssignedQuestionCommand>,
)

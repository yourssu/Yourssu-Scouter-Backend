package com.yourssu.scouter.recruiting.interview.business.dto

data class SaveInterviewMemoCommand(
    val questionnaireQuestionId: Long,
    val memo: String,
)

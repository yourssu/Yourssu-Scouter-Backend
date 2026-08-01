package com.yourssu.scouter.recruiting.interview.business.dto

data class SaveInterviewMemoCommand(
    val assignedQuestionId: Long,
    val memo: String,
)

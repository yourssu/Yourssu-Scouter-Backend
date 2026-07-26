package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult

data class SaveInterviewEvaluationCommand(
    val applicantId: Long,
    val evaluatorUserId: Long,
    val items: List<SaveInterviewEvaluationItemCommand>,
    val overallComment: String = "",
    val result: InterviewResult,
    val submit: Boolean,
)

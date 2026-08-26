package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult

data class OtherInterviewEvaluationDto(
    val evaluatorId: Long,
    val evaluatorName: String,
    val evaluatorNickname: String,
    val totalScore: Int,
    val result: InterviewResult,
    val overallComment: String,
    val items: List<OtherInterviewEvaluationItemDto>,
)

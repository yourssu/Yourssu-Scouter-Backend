package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import java.time.LocalDateTime

data class InterviewEvaluationDto(
    val totalScore: Int,
    val items: List<InterviewEvaluationItemDto>,
    val overallComment: String,
    val result: InterviewResult,
    val submittedAt: LocalDateTime?,
)


package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import java.time.Instant

data class InterviewEvaluationDto(
    val totalScore: Int,
    val items: List<InterviewEvaluationItemDto>,
    val result: InterviewResult,
    val submittedAt: Instant?,
)

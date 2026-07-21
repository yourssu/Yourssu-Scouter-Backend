package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.evaluation.implement.DocumentResult
import java.time.Instant

data class DocumentEvaluationDto(
    val totalScore: Int,
    val items: List<DocumentEvaluationItemDto>,
    val overallComment: String,
    val result: DocumentResult,
    val submittedAt: Instant?,
)

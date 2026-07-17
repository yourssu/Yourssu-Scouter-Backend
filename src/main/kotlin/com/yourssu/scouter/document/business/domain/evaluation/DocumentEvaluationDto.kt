package com.yourssu.scouter.document.business.domain.evaluation

import com.yourssu.scouter.document.implement.domain.evaluation.DocumentResult
import java.time.Instant

data class DocumentEvaluationDto(
    val totalScore: Int,
    val items: List<DocumentEvaluationItemDto>,
    val overallComment: String,
    val result: DocumentResult,
    val submittedAt: Instant?,
)

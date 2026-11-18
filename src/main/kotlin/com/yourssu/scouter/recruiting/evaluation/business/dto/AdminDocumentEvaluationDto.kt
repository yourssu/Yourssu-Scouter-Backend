package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.evaluation.implement.DocumentResult
import java.time.Instant

data class AdminDocumentEvaluationDto(
    val evaluatorUserId: Long,
    val evaluatorName: String,
    val evaluatorEmail: String,
    val totalScore: Int,
    val result: DocumentResult,
    val overallComment: String,
    val submittedAt: Instant?,
    val items: List<AdminDocumentEvaluationItemDto>,
)

data class AdminDocumentEvaluationItemDto(
    val sectionId: Long,
    val question: String,
    val maxScore: Int,
    val score: Int,
    val memo: String,
)

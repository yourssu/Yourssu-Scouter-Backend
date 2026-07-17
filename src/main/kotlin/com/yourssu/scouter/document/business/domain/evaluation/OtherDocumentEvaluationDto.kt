package com.yourssu.scouter.document.business.domain.evaluation

import com.yourssu.scouter.document.implement.domain.evaluation.DocumentResult

data class OtherDocumentEvaluationItemDto(
    val sectionId: Long,
    val score: Int,
    val memo: String,
)

data class OtherDocumentEvaluationDto(
    val evaluatorId: Long,
    val evaluatorName: String,
    val totalScore: Int,
    val result: DocumentResult,
    val overallComment: String,
    val items: List<OtherDocumentEvaluationItemDto>,
)

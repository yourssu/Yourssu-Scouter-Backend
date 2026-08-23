package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.evaluation.implement.DocumentResult

data class OtherDocumentEvaluationItemDto(
    val sectionId: Long,
    val score: Int,
    val memo: String,
)

data class OtherDocumentEvaluationDto(
    val evaluatorId: Long,
    val evaluatorName: String,
    val evaluatorNickname: String,
    val totalScore: Int,
    val result: DocumentResult,
    val overallComment: String,
    val items: List<OtherDocumentEvaluationItemDto>,
)

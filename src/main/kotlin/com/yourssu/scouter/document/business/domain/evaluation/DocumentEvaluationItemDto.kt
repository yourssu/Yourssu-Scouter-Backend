package com.yourssu.scouter.document.business.domain.evaluation

data class DocumentEvaluationItemDto(
    val sectionId: Long,
    val question: String,
    val maxScore: Int,
    val score: Int,
    val memo: String,
)

package com.yourssu.scouter.recruiting.evaluation.business.dto

data class DocumentEvaluationItemDto(
    val sectionId: Long,
    val question: String,
    val maxScore: Int,
    val score: Int,
    val memo: String,
)

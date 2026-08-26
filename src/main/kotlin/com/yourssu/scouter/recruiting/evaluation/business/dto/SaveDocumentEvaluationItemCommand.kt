package com.yourssu.scouter.recruiting.evaluation.business.dto

data class SaveDocumentEvaluationItemCommand(
    val sectionId: Long,
    val score: Int,
    val memo: String,
)

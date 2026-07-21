package com.yourssu.scouter.recruiting.evaluation.business

data class SaveDocumentEvaluationItemCommand(
    val sectionId: Long,
    val score: Int,
    val memo: String,
)

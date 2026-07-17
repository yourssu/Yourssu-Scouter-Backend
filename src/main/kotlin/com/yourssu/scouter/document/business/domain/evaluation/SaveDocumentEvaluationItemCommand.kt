package com.yourssu.scouter.document.business.domain.evaluation

data class SaveDocumentEvaluationItemCommand(
    val sectionId: Long,
    val score: Int,
    val memo: String,
)

package com.yourssu.scouter.document.business.domain.evaluation

import com.yourssu.scouter.document.implement.domain.evaluation.DocumentResult

data class SaveDocumentEvaluationCommand(
    val applicantId: Long,
    val evaluatorUserId: Long,
    val items: List<SaveDocumentEvaluationItemCommand>,
    val overallComment: String,
    val result: DocumentResult,
    val submit: Boolean,
)

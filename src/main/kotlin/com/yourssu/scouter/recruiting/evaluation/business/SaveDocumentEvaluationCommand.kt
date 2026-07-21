package com.yourssu.scouter.recruiting.evaluation.business

import com.yourssu.scouter.recruiting.evaluation.implement.DocumentResult

data class SaveDocumentEvaluationCommand(
    val applicantId: Long,
    val evaluatorUserId: Long,
    val items: List<SaveDocumentEvaluationItemCommand>,
    val overallComment: String,
    val result: DocumentResult,
    val submit: Boolean,
)

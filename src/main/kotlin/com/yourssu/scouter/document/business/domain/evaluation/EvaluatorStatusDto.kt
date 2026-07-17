package com.yourssu.scouter.document.business.domain.evaluation

import com.yourssu.scouter.document.implement.domain.evaluation.EvaluationStatus

data class EvaluatorStatusDto(
    val userId: Long,
    val name: String,
    val status: EvaluationStatus,
)

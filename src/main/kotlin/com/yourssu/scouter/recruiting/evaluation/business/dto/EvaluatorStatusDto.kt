package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.evaluation.implement.EvaluationStatus

data class EvaluatorStatusDto(
    val userId: Long,
    val name: String,
    val status: EvaluationStatus,
)

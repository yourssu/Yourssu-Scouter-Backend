package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.evaluation.implement.EvaluationStatus

data class EvaluatorStatusDto(
    val memberId: Long,
    val userId: Long?,
    val name: String,
    val nickname: String,
    val status: EvaluationStatus,
)

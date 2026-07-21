package com.yourssu.scouter.recruiting.evaluation.application

import com.yourssu.scouter.recruiting.evaluation.business.dto.EvaluatorStatusDto
import com.yourssu.scouter.recruiting.evaluation.implement.EvaluationStatus

data class ReadEvaluatorStatusResponse(
    val userId: Long,
    val name: String,
    val status: EvaluationStatus,
) {
    companion object {
        fun from(dto: EvaluatorStatusDto): ReadEvaluatorStatusResponse =
            ReadEvaluatorStatusResponse(dto.userId, dto.name, dto.status)
    }
}

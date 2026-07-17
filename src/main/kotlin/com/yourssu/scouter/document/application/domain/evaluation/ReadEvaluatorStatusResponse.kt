package com.yourssu.scouter.document.application.domain.evaluation

import com.yourssu.scouter.document.business.domain.evaluation.EvaluatorStatusDto
import com.yourssu.scouter.document.implement.domain.evaluation.EvaluationStatus

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

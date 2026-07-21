package com.yourssu.scouter.recruiting.evaluation.application

import com.yourssu.scouter.recruiting.evaluation.business.dto.OtherDocumentEvaluationDto
import com.yourssu.scouter.recruiting.evaluation.business.dto.OtherDocumentEvaluationItemDto
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentResult

data class ReadOtherDocumentEvaluationItemResponse(
    val sectionId: Long,
    val score: Int,
    val memo: String,
) {
    companion object {
        fun from(dto: OtherDocumentEvaluationItemDto): ReadOtherDocumentEvaluationItemResponse =
            ReadOtherDocumentEvaluationItemResponse(dto.sectionId, dto.score, dto.memo)
    }
}

data class ReadOtherDocumentEvaluationResponse(
    val evaluatorId: Long,
    val evaluatorName: String,
    val totalScore: Int,
    val result: DocumentResult,
    val overallComment: String,
    val items: List<ReadOtherDocumentEvaluationItemResponse>,
) {
    companion object {
        fun from(dto: OtherDocumentEvaluationDto): ReadOtherDocumentEvaluationResponse = ReadOtherDocumentEvaluationResponse(
            evaluatorId = dto.evaluatorId,
            evaluatorName = dto.evaluatorName,
            totalScore = dto.totalScore,
            result = dto.result,
            overallComment = dto.overallComment,
            items = dto.items.map(ReadOtherDocumentEvaluationItemResponse::from),
        )
    }
}

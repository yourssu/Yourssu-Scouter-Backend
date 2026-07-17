package com.yourssu.scouter.document.application.domain.evaluation

import com.yourssu.scouter.document.business.domain.evaluation.DocumentEvaluationDto
import com.yourssu.scouter.document.business.domain.evaluation.DocumentEvaluationItemDto
import com.yourssu.scouter.document.implement.domain.evaluation.DocumentResult
import java.time.Instant

data class ReadDocumentEvaluationItemResponse(
    val sectionId: Long,
    val question: String,
    val maxScore: Int,
    val score: Int,
    val memo: String,
) {
    companion object {
        fun from(dto: DocumentEvaluationItemDto): ReadDocumentEvaluationItemResponse = ReadDocumentEvaluationItemResponse(
            sectionId = dto.sectionId,
            question = dto.question,
            maxScore = dto.maxScore,
            score = dto.score,
            memo = dto.memo,
        )
    }
}

data class ReadDocumentEvaluationResponse(
    val totalScore: Int,
    val items: List<ReadDocumentEvaluationItemResponse>,
    val overallComment: String,
    val result: DocumentResult,
    val submittedAt: Instant?,
) {
    companion object {
        fun from(dto: DocumentEvaluationDto): ReadDocumentEvaluationResponse = ReadDocumentEvaluationResponse(
            totalScore = dto.totalScore,
            items = dto.items.map(ReadDocumentEvaluationItemResponse::from),
            overallComment = dto.overallComment,
            result = dto.result,
            submittedAt = dto.submittedAt,
        )
    }
}

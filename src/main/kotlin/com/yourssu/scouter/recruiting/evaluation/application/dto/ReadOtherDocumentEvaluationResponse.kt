package com.yourssu.scouter.recruiting.evaluation.application.dto

import com.yourssu.scouter.recruiting.evaluation.business.dto.OtherDocumentEvaluationDto
import com.yourssu.scouter.recruiting.evaluation.business.dto.OtherDocumentEvaluationItemDto
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentResult
import io.swagger.v3.oas.annotations.media.Schema

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

@Schema(description = "다른 평가자의 서류 평가 조회 응답")
data class ReadOtherDocumentEvaluationResponse(
    @field:Schema(description = "평가자 사용자 ID", example = "23", nullable = false)
    val evaluatorId: Long,
    @field:Schema(description = "평가자 이름", example = "홍길동", nullable = false)
    val evaluatorName: String,
    @field:Schema(description = "평가자 닉네임", example = "gildong(길동)", nullable = false)
    val evaluatorNickname: String,
    val totalScore: Int,
    val result: DocumentResult,
    val overallComment: String,
    val items: List<ReadOtherDocumentEvaluationItemResponse>,
) {
    companion object {
        fun from(dto: OtherDocumentEvaluationDto): ReadOtherDocumentEvaluationResponse = ReadOtherDocumentEvaluationResponse(
            evaluatorId = dto.evaluatorId,
            evaluatorName = dto.evaluatorName,
            evaluatorNickname = dto.evaluatorNickname,
            totalScore = dto.totalScore,
            result = dto.result,
            overallComment = dto.overallComment,
            items = dto.items.map(ReadOtherDocumentEvaluationItemResponse::from),
        )
    }
}

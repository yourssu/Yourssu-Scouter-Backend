package com.yourssu.scouter.recruiting.evaluation.application.dto

import com.yourssu.scouter.recruiting.evaluation.business.dto.OtherInterviewEvaluationDto
import com.yourssu.scouter.recruiting.evaluation.business.dto.OtherInterviewEvaluationItemDto
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult

data class ReadOtherInterviewEvaluationItemResponse(
    val itemId: Long,
    val score: Int,
) {
    companion object {
        fun from(dto: OtherInterviewEvaluationItemDto): ReadOtherInterviewEvaluationItemResponse =
            ReadOtherInterviewEvaluationItemResponse(dto.evaluationItemId, dto.score)
    }
}

data class ReadOtherInterviewEvaluationResponse(
    val evaluatorId: Long,
    val evaluatorName: String,
    val totalScore: Int,
    val result: InterviewResult,
    val overallComment: String,
    val items: List<ReadOtherInterviewEvaluationItemResponse>,
) {
    companion object {
        fun from(dto: OtherInterviewEvaluationDto): ReadOtherInterviewEvaluationResponse = ReadOtherInterviewEvaluationResponse(
            evaluatorId = dto.evaluatorId,
            evaluatorName = dto.evaluatorName,
            totalScore = dto.totalScore,
            result = dto.result,
            overallComment = dto.overallComment,
            items = dto.items.map(ReadOtherInterviewEvaluationItemResponse::from),
        )
    }
}

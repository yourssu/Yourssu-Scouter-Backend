package com.yourssu.scouter.recruiting.evaluation.application.dto

import com.yourssu.scouter.recruiting.evaluation.business.dto.InterviewEvaluationDto
import com.yourssu.scouter.recruiting.evaluation.business.dto.InterviewEvaluationItemDto
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import java.time.Instant

data class ReadInterviewEvaluationItemResponse(
    val evaluationItemId: Long,
    val keyword: String,
    val rubricType: RubricGroupType,
    val maxScore: Int,
    val score: Int,
) {
    companion object {
        fun from(dto: InterviewEvaluationItemDto): ReadInterviewEvaluationItemResponse = ReadInterviewEvaluationItemResponse(
            evaluationItemId = dto.evaluationItemId,
            keyword = dto.keyword,
            rubricType = dto.rubricType,
            maxScore = dto.maxScore,
            score = dto.score
        )
    }
}

data class ReadInterviewEvaluationResponse(
    val totalScore: Int,
    val items: List<ReadInterviewEvaluationItemResponse>,
    val result: InterviewResult,
    val submittedAt: Instant?,
) {
    companion object {
        fun from(dto: InterviewEvaluationDto): ReadInterviewEvaluationResponse = ReadInterviewEvaluationResponse(
            totalScore = dto.totalScore,
            items = dto.items.map(ReadInterviewEvaluationItemResponse::from),
            result = dto.result,
            submittedAt = dto.submittedAt
        )
    }
}

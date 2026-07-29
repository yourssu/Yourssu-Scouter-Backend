package com.yourssu.scouter.recruiting.evaluation.application.dto

import com.yourssu.scouter.recruiting.evaluation.business.dto.InterviewEvaluationDto
import com.yourssu.scouter.recruiting.evaluation.business.dto.InterviewEvaluationItemDto
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import java.time.LocalDateTime

data class ReadInterviewEvaluationGroupResponse(
    val group: String,
    val items: List<ReadInterviewEvaluationItemResponse>
)

data class ReadInterviewEvaluationItemResponse(
    val itemId: Long,
    val itemTitle: String,
    val maxScore: Int,
    val score: Int,
)

data class ReadInterviewEvaluationResponse(
    val totalScore: Int,
    val groups: List<ReadInterviewEvaluationGroupResponse>,
    val overallComment: String,
    val result: InterviewResult,
    val submittedAt: LocalDateTime?,
) {
    companion object {
        fun from(dto: InterviewEvaluationDto): ReadInterviewEvaluationResponse {
            val itemsByGroup = dto.items.groupBy { it.rubricType }
            val groups = listOf(RubricGroupType.CULTURE, RubricGroupType.TEAM, RubricGroupType.JOB).map { groupType ->
                val items = itemsByGroup[groupType] ?: emptyList()
                ReadInterviewEvaluationGroupResponse(
                    group = when (groupType) {
                        RubricGroupType.CULTURE -> "CULTURE_FIT"
                        RubricGroupType.TEAM -> "TEAM_FIT"
                        RubricGroupType.JOB -> "JOB_FIT"
                    },
                    items = items.map {
                        ReadInterviewEvaluationItemResponse(
                            itemId = it.evaluationItemId,
                            itemTitle = it.keyword,
                            maxScore = it.maxScore,
                            score = it.score
                        )
                    }
                )
            }

            return ReadInterviewEvaluationResponse(
                totalScore = dto.totalScore,
                groups = groups,
                overallComment = dto.overallComment,
                result = dto.result,
                submittedAt = dto.submittedAt
            )
        }
    }
}


package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import java.time.LocalDateTime

data class AdminInterviewEvaluationDto(
    val evaluatorUserId: Long,
    val evaluatorName: String,
    val evaluatorEmail: String,
    val totalScore: Int,
    val result: InterviewResult?,
    val overallComment: String,
    val submittedAt: LocalDateTime?,
    val items: List<AdminInterviewEvaluationItemDto>,
)

data class AdminInterviewEvaluationItemDto(
    val evaluationItemId: Long,
    val keyword: String,
    val rubricType: RubricGroupType?,
    val maxScore: Int,
    val score: Int,
)

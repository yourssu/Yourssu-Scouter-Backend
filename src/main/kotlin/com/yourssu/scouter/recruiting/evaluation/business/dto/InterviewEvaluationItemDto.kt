package com.yourssu.scouter.recruiting.evaluation.business.dto

import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType

data class InterviewEvaluationItemDto(
    val evaluationItemId: Long,
    val keyword: String,
    val rubricType: RubricGroupType,
    val maxScore: Int,
    val score: Int,
)

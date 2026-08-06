package com.yourssu.scouter.recruiting.evaluation.implement

import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType


class InterviewEvaluationItem(
    val id: Long? = null,
    val interviewRequirementId: Long? = null,
    val keyword: String,
    val rubricType: RubricGroupType,
    val maxScore: Int,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterviewEvaluationItem

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

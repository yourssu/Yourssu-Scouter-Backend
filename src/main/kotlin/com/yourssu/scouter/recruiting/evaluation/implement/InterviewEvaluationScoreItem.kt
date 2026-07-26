package com.yourssu.scouter.recruiting.evaluation.implement

class InterviewEvaluationScoreItem(
    val id: Long? = null,
    val evaluationItemId: Long, // InterviewEvaluationItemEntity의 ID
    val score: Int,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterviewEvaluationScoreItem

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
package com.yourssu.scouter.recruiting.evaluation.implement

import java.time.Instant

class InterviewEvaluation(
    val id: Long? = null,
    val applicantId: Long,
    val evaluatorUserId: Long, // Entity의 memberId
    val items: List<InterviewEvaluationScoreItem>,
    val result: InterviewResult = InterviewResult.PENDING,
    val submittedAt: Instant? = null,
) {

    fun totalScore(): Int = items.sumOf { it.score }

    fun isSubmitted(): Boolean = submittedAt != null

    fun update(
        items: List<InterviewEvaluationScoreItem>,
        result: InterviewResult,
        submittedAt: Instant? = this.submittedAt
    ): InterviewEvaluation {
        return InterviewEvaluation(
            id = this.id,
            applicantId = this.applicantId,
            evaluatorUserId = this.evaluatorUserId,
            items = items,
            result = result,
            submittedAt = submittedAt
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InterviewEvaluation

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
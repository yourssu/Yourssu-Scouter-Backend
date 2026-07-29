package com.yourssu.scouter.recruiting.evaluation.implement

class InterviewEvaluation(
    val id: Long? = null,
    val applicantId: Long,
    val evaluatorUserId: Long, // Entity의 memberId
    val items: List<InterviewEvaluationScoreItem>
) {

    fun totalScore(): Int = items.sumOf { it.score }

    fun update(
        items: List<InterviewEvaluationScoreItem>
    ): InterviewEvaluation {
        return InterviewEvaluation(
            id = this.id,
            applicantId = this.applicantId,
            evaluatorUserId = this.evaluatorUserId,
            items = items
        )
    }
}
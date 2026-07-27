package com.yourssu.scouter.recruiting.evaluation.implement

import java.time.LocalDateTime

class FinalEvaluation(
    val id: Long? = null,
    val memberId: Long,
    val interviewRubricId: Long,
    val applicantId: Long,
    val overallComment: String = "",
    val interviewResult: InterviewResult = InterviewResult.PENDING,
    val score: Int,
    val submit: Boolean = false,
    val submittedAt: LocalDateTime? = null
) {
    fun isSubmitted(): Boolean = submit
}


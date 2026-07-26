package com.yourssu.scouter.recruiting.evaluation.application.dto

import com.yourssu.scouter.recruiting.evaluation.business.dto.SaveInterviewEvaluationCommand
import com.yourssu.scouter.recruiting.evaluation.business.dto.SaveInterviewEvaluationItemCommand
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import jakarta.validation.constraints.NotNull

data class SaveInterviewEvaluationItemRequest(
    @field:NotNull
    val itemId: Long?,

    @field:NotNull
    val score: Int?,
) {
    fun toCommand(): SaveInterviewEvaluationItemCommand = SaveInterviewEvaluationItemCommand(
        evaluationItemId = itemId!!,
        score = score!!,
    )
}

data class SaveInterviewEvaluationRequest(
    val items: List<SaveInterviewEvaluationItemRequest> = emptyList(),

    val overallComment: String = "",

    @field:NotNull
    val result: InterviewResult?,

    @field:NotNull
    val submit: Boolean?,
) {
    fun toCommand(applicantId: Long, evaluatorUserId: Long): SaveInterviewEvaluationCommand = SaveInterviewEvaluationCommand(
        applicantId = applicantId,
        evaluatorUserId = evaluatorUserId,
        items = items.map { it.toCommand() },
        overallComment = overallComment,
        result = result!!,
        submit = submit!!,
    )
}

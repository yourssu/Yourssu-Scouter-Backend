package com.yourssu.scouter.recruiting.evaluation.application.dto

import com.yourssu.scouter.recruiting.evaluation.business.dto.SaveInterviewEvaluationCommand
import com.yourssu.scouter.recruiting.evaluation.business.dto.SaveInterviewEvaluationItemCommand
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import jakarta.validation.constraints.NotEmpty
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
    @field:NotEmpty(message = "평가 항목 점수 리스트는 비어있을 수 없습니다.")
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

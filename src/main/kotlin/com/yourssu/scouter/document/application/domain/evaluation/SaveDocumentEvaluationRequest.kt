package com.yourssu.scouter.document.application.domain.evaluation

import com.yourssu.scouter.document.business.domain.evaluation.SaveDocumentEvaluationCommand
import com.yourssu.scouter.document.business.domain.evaluation.SaveDocumentEvaluationItemCommand
import com.yourssu.scouter.document.implement.domain.evaluation.DocumentResult
import jakarta.validation.constraints.NotNull

data class SaveDocumentEvaluationItemRequest(

    @field:NotNull
    val sectionId: Long?,

    @field:NotNull
    val score: Int?,

    val memo: String = "",
) {
    fun toCommand(): SaveDocumentEvaluationItemCommand = SaveDocumentEvaluationItemCommand(
        sectionId = sectionId!!,
        score = score!!,
        memo = memo,
    )
}

data class SaveDocumentEvaluationRequest(

    val items: List<SaveDocumentEvaluationItemRequest> = emptyList(),

    val overallComment: String = "",

    @field:NotNull
    val result: DocumentResult?,

    @field:NotNull
    val submit: Boolean?,
) {
    fun toCommand(applicantId: Long, evaluatorUserId: Long): SaveDocumentEvaluationCommand = SaveDocumentEvaluationCommand(
        applicantId = applicantId,
        evaluatorUserId = evaluatorUserId,
        items = items.map { it.toCommand() },
        overallComment = overallComment,
        result = result!!,
        submit = submit!!,
    )
}

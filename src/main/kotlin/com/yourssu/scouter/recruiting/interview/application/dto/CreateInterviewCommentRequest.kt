package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.comment.business.dto.CreateDocumentCommentCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateInterviewCommentRequest(

    @field:NotNull
    val sectionId: Long?,

    @field:NotBlank
    val content: String?,
) {
    fun toCommand(applicantId: Long, authorUserId: Long): CreateDocumentCommentCommand = CreateDocumentCommentCommand(
        applicantId = applicantId,
        sectionId = sectionId!!,
        authorUserId = authorUserId,
        content = content!!,
        parentCommentId = null,
    )
}

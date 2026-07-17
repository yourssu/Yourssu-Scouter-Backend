package com.yourssu.scouter.document.application.domain.comment

import com.yourssu.scouter.document.business.domain.comment.CreateDocumentCommentCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateDocumentCommentRequest(

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
    )
}

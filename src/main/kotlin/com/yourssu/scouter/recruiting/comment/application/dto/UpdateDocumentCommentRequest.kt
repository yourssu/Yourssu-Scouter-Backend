package com.yourssu.scouter.recruiting.comment.application.dto

import com.yourssu.scouter.recruiting.comment.business.dto.UpdateDocumentCommentCommand
import jakarta.validation.constraints.NotBlank

data class UpdateDocumentCommentRequest(

    @field:NotBlank
    val content: String?,
) {
    fun toCommand(commentId: Long, requesterUserId: Long): UpdateDocumentCommentCommand = UpdateDocumentCommentCommand(
        commentId = commentId,
        requesterUserId = requesterUserId,
        content = content!!,
    )
}

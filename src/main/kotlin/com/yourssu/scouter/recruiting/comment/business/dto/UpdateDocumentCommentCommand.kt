package com.yourssu.scouter.recruiting.comment.business.dto

data class UpdateDocumentCommentCommand(
    val commentId: Long,
    val requesterUserId: Long,
    val content: String,
)

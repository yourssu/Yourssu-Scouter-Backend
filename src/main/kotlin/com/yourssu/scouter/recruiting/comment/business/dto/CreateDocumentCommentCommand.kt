package com.yourssu.scouter.recruiting.comment.business.dto

data class CreateDocumentCommentCommand(
    val applicantId: Long,
    val sectionId: Long,
    val authorUserId: Long,
    val content: String,
    val parentCommentId: Long? = null,
)

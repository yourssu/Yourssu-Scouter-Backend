package com.yourssu.scouter.document.business.domain.comment

data class CreateDocumentCommentCommand(
    val applicantId: Long,
    val sectionId: Long,
    val authorUserId: Long,
    val content: String,
)

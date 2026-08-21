package com.yourssu.scouter.recruiting.comment.implement

import java.time.Instant

class DocumentComment(
    val id: Long? = null,
    val applicantId: Long,
    val sectionId: Long,
    val authorUserId: Long,
    val content: String,
    val parentCommentId: Long? = null,
    val category: CommentCategory = CommentCategory.DOCUMENT,
    val isEdited: Boolean = false,
    val createdAt: Instant = Instant.now(),
) {

    val isReply: Boolean
        get() = parentCommentId != null

    fun edit(newContent: String): DocumentComment = DocumentComment(
        id = id,
        applicantId = applicantId,
        sectionId = sectionId,
        authorUserId = authorUserId,
        content = newContent,
        parentCommentId = parentCommentId,
        category = category,
        isEdited = true,
        createdAt = createdAt,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentComment

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

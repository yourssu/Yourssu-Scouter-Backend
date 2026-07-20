package com.yourssu.scouter.document.implement.domain.comment

import java.time.Instant

class DocumentComment(
    val id: Long? = null,
    val applicantId: Long,
    val sectionId: Long,
    val authorUserId: Long,
    val content: String,
    val parentCommentId: Long? = null,
    val createdAt: Instant? = null,
) {

    val isReply: Boolean
        get() = parentCommentId != null

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

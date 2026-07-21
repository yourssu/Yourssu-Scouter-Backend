package com.yourssu.scouter.recruiting.comment.implement

import java.time.Instant

class DocumentComment(
    val id: Long? = null,
    val applicantId: Long,
    val sectionId: Long,
    val authorUserId: Long,
    val content: String,
    val createdAt: Instant? = null,
) {

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

package com.yourssu.scouter.recruiting.comment.business

import com.yourssu.scouter.recruiting.comment.implement.DocumentComment
import java.time.Instant

data class DocumentCommentAuthorDto(
    val userId: Long,
    val nickname: String,
    val part: String,
)

data class DocumentCommentDto(
    val commentId: Long,
    val sectionId: Long,
    val content: String,
    val author: DocumentCommentAuthorDto,
    val createdAt: Instant?,
) {
    companion object {
        fun of(comment: DocumentComment, author: DocumentCommentAuthorDto): DocumentCommentDto = DocumentCommentDto(
            commentId = comment.id!!,
            sectionId = comment.sectionId,
            content = comment.content,
            author = author,
            createdAt = comment.createdAt,
        )
    }
}

package com.yourssu.scouter.document.business.domain.comment

import com.yourssu.scouter.document.implement.domain.comment.DocumentComment
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

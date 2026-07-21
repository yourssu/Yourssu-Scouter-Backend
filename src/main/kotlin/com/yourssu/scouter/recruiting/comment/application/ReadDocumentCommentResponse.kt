package com.yourssu.scouter.recruiting.comment.application

import com.yourssu.scouter.recruiting.comment.business.DocumentCommentAuthorDto
import com.yourssu.scouter.recruiting.comment.business.DocumentCommentDto
import java.time.Instant

data class ReadDocumentCommentAuthorResponse(
    val userId: Long,
    val nickname: String,
    val part: String,
) {
    companion object {
        fun from(dto: DocumentCommentAuthorDto): ReadDocumentCommentAuthorResponse =
            ReadDocumentCommentAuthorResponse(dto.userId, dto.nickname, dto.part)
    }
}

data class ReadDocumentCommentResponse(
    val commentId: Long,
    val sectionId: Long,
    val content: String,
    val author: ReadDocumentCommentAuthorResponse,
    val parentCommentId: Long?,
    val createdAt: Instant?,
) {
    companion object {
        fun from(dto: DocumentCommentDto): ReadDocumentCommentResponse = ReadDocumentCommentResponse(
            commentId = dto.commentId,
            sectionId = dto.sectionId,
            content = dto.content,
            author = ReadDocumentCommentAuthorResponse.from(dto.author),
            parentCommentId = dto.parentCommentId,
            createdAt = dto.createdAt,
        )
    }
}

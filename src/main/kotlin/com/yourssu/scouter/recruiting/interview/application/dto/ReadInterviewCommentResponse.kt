package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.comment.application.dto.ReadDocumentCommentAuthorResponse
import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentDto
import java.time.Instant

data class ReadInterviewCommentResponse(
    val commentId: Long,
    val sectionId: Long,
    val content: String,
    val author: ReadDocumentCommentAuthorResponse,
    val createdAt: Instant?,
) {
    companion object {
        fun from(dto: DocumentCommentDto): ReadInterviewCommentResponse = ReadInterviewCommentResponse(
            commentId = dto.commentId,
            sectionId = dto.sectionId,
            content = dto.content,
            author = ReadDocumentCommentAuthorResponse.from(dto.author),
            createdAt = dto.createdAt,
        )
    }
}

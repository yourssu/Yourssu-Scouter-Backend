package com.yourssu.scouter.recruiting.comment.application.dto

import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentAuthorDto
import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentDto
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "코멘트 작성자 정보")
data class ReadDocumentCommentAuthorResponse(

    @field:Schema(
        description = "작성자 회원의 ID (동아리 부원 ID)",
        example = "101",
        types = ["integer", "null"],
        format = "int64",
    )
    val memberId: Long?,

    @field:Schema(
        description = "작성자 닉네임 (영어 닉네임 또는 이름)",
        example = "Teddy",
        types = ["string"],
    )
    val nickname: String,

    @field:Schema(
        description = "작성자 파트명",
        example = "Backend",
        types = ["string"],
    )
    val part: String,
) {
    companion object {
        fun from(dto: DocumentCommentAuthorDto): ReadDocumentCommentAuthorResponse =
            ReadDocumentCommentAuthorResponse(dto.memberId, dto.nickname, dto.part)
    }
}

data class ReadDocumentCommentResponse(
    val commentId: Long,
    val sectionId: Long,
    val content: String,
    val author: ReadDocumentCommentAuthorResponse,
    val parentCommentId: Long?,
    val isEdited: Boolean,
    val createdAt: Instant?,
) {
    companion object {
        fun from(dto: DocumentCommentDto): ReadDocumentCommentResponse = ReadDocumentCommentResponse(
            commentId = dto.commentId,
            sectionId = dto.sectionId,
            content = dto.content,
            author = ReadDocumentCommentAuthorResponse.from(dto.author),
            parentCommentId = dto.parentCommentId,
            isEdited = dto.isEdited,
            createdAt = dto.createdAt,
        )
    }
}

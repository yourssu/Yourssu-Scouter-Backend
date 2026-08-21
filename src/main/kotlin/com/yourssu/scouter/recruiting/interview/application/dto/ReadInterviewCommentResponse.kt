package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.comment.application.dto.ReadDocumentCommentAuthorResponse
import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentDto
import com.yourssu.scouter.recruiting.interview.business.dto.QuestionInterviewCommentsDto
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "질문 단위 면접 질문 코멘트 목록 응답")
data class QuestionInterviewCommentsResponse(

    @field:Schema(
        description = "할당된 면접 질문 ID (assignedQuestionId)",
        example = "1",
        types = ["integer"],
        format = "int64",
    )
    val sectionId: Long,

    @field:Schema(description = "해당 질문에 작성된 코멘트 목록")
    val comments: List<ReadInterviewCommentResponse>,
) {
    companion object {
        fun from(dto: QuestionInterviewCommentsDto): QuestionInterviewCommentsResponse = QuestionInterviewCommentsResponse(
            sectionId = dto.sectionId,
            comments = dto.comments.map(ReadInterviewCommentResponse::from),
        )
    }
}

@Schema(description = "면접 질문 코멘트 항목 응답")
data class ReadInterviewCommentResponse(

    @field:Schema(
        description = "코멘트 ID",
        example = "10",
        types = ["integer"],
        format = "int64",
    )
    val commentId: Long,

    @field:Schema(
        description = "할당된 면접 질문 ID (assignedQuestionId)",
        example = "1",
        types = ["integer"],
        format = "int64",
    )
    val sectionId: Long,

    @field:Schema(
        description = "면접 질문 코멘트 내용",
        example = "지원자의 답변 내용, 추가 질문사항 등",
        types = ["string"],
    )
    val content: String,

    @field:Schema(description = "작성자 정보")
    val author: ReadDocumentCommentAuthorResponse,

    @field:Schema(
        description = "작성 일시",
        example = "2026-08-19T03:00:00Z",
        types = ["string"],
        format = "date-time",
    )
    val createdAt: Instant,
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

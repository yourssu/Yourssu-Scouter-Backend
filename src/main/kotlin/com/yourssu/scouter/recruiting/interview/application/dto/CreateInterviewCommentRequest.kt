package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.comment.business.dto.CreateDocumentCommentCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "면접 질문 코멘트 생성 요청")
data class CreateInterviewCommentRequest(

    @field:Schema(
        description = "할당된 면접 질문 ID (assignedQuestionId)",
        example = "1",
        types = ["integer"],
        format = "int64",
    )
    @field:NotNull
    val sectionId: Long?,

    @field:Schema(
        description = "면접 질문 코멘트 내용",
        example = "프로젝트 경험 관련 구체적인 추가 질문 필요",
        types = ["string"],
    )
    @field:NotBlank
    val content: String?,
) {
    fun toCommand(applicantId: Long, authorUserId: Long): CreateDocumentCommentCommand = CreateDocumentCommentCommand(
        applicantId = applicantId,
        sectionId = sectionId!!,
        authorUserId = authorUserId,
        content = content!!,
        parentCommentId = null,
    )
}

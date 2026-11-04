package com.yourssu.scouter.recruiting.comment.application

import com.yourssu.scouter.recruiting.comment.application.dto.CreateDocumentCommentRequest

import com.yourssu.scouter.recruiting.comment.application.dto.ReadDocumentCommentResponse

import com.yourssu.scouter.recruiting.comment.application.dto.UpdateDocumentCommentRequest

import com.yourssu.scouter.auth.support.annotation.AuthUser
import com.yourssu.scouter.auth.support.resolver.AuthUserInfo
import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentDto
import com.yourssu.scouter.recruiting.comment.business.CommentService
import com.yourssu.scouter.recruiting.comment.application.dto.CreateInterviewCommentRequest
import com.yourssu.scouter.recruiting.comment.application.dto.QuestionInterviewCommentsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@Tag(name = "코멘트")
@RestController
class CommentController(
    private val commentService: CommentService,
) {

    @Operation(summary = "면접 질문 메모 목록 조회", description = "지원자의 할당된 질문별로 코멘트를 그룹화하여 조회합니다.")
    @GetMapping("/applicants/{applicantId}/interviews/comments")
    fun readAllInterviewComments(
        @PathVariable applicantId: Long,
    ): ResponseEntity<List<QuestionInterviewCommentsResponse>> {
        val comments = commentService.readAllInterviewCommentsByApplicantId(applicantId)

        return ResponseEntity.ok(comments.map(QuestionInterviewCommentsResponse::from))
    }

    @Operation(summary = "면접 질문 메모 작성")
    @ApiResponse(
        description = "CREATED", responseCode = "201", headers = [
            Header(name = "Location", description = "/applicants/{applicantId}/interviews/comments/{commentId}"),
        ],
    )
    @PostMapping("/applicants/{applicantId}/interviews/comments")
    fun createInterviewComment(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
        @RequestBody @Valid request: CreateInterviewCommentRequest,
    ): ResponseEntity<Unit> {
        val dto: DocumentCommentDto = commentService.createInterviewComment(request.toCommand(applicantId, authUserInfo.userId))

        return ResponseEntity.created(URI.create("/applicants/$applicantId/interviews/comments/${dto.commentId}")).build()
    }

    @Operation(summary = "면접 질문 코멘트 삭제", description = "본인이 작성한 코멘트만 삭제 가능합니다.")
    @DeleteMapping("/applicants/{applicantId}/interviews/comments/{commentId}")
    fun deleteInterviewComment(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
        @PathVariable commentId: Long,
    ): ResponseEntity<Unit> {
        commentService.deleteInterviewCommentById(commentId, authUserInfo.userId)

        return ResponseEntity.ok().build()
    }
    @Operation(summary = "인라인 코멘트 조회")
    @GetMapping("/applicants/{applicantId}/documents/comments")
    fun readAllDocumentComments(
        @PathVariable applicantId: Long,
    ): ResponseEntity<List<ReadDocumentCommentResponse>> {
        val comments = commentService.readAllDocumentCommentsByApplicantId(applicantId)

        return ResponseEntity.ok(comments.map(ReadDocumentCommentResponse::from))
    }

    @Operation(summary = "인라인 코멘트 작성")
    @ApiResponse(
        description = "CREATED", responseCode = "201", headers = [
            Header(name = "Location", description = "/applicants/{applicantId}/documents/comments/{commentId}"),
        ],
    )
    @PostMapping("/applicants/{applicantId}/documents/comments")
    fun createDocumentComment(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
        @RequestBody @Valid request: CreateDocumentCommentRequest,
    ): ResponseEntity<Unit> {
        val dto: DocumentCommentDto = commentService.createDocumentComment(request.toCommand(applicantId, authUserInfo.userId))

        return ResponseEntity.created(URI.create("/applicants/$applicantId/documents/comments/${dto.commentId}")).build()
    }

    @Operation(summary = "인라인 코멘트 수정", description = "본인이 작성한 코멘트만 수정 가능합니다.")
    @PatchMapping("/applicants/{applicantId}/documents/comments/{commentId}")
    fun updateDocumentComment(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
        @PathVariable commentId: Long,
        @RequestBody @Valid request: UpdateDocumentCommentRequest,
    ): ResponseEntity<ReadDocumentCommentResponse> {
        val dto = commentService.updateDocumentComment(request.toCommand(commentId, authUserInfo.userId))

        return ResponseEntity.ok(ReadDocumentCommentResponse.from(dto))
    }

    @Operation(summary = "인라인 코멘트 삭제", description = "본인이 작성한 코멘트만 삭제 가능합니다.")
    @DeleteMapping("/applicants/{applicantId}/documents/comments/{commentId}")
    fun deleteDocumentComment(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
        @PathVariable commentId: Long,
    ): ResponseEntity<Unit> {
        commentService.deleteDocumentCommentById(commentId, authUserInfo.userId)

        return ResponseEntity.ok().build()
    }
}

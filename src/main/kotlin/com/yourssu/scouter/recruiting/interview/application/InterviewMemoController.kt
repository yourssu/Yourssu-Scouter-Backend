package com.yourssu.scouter.recruiting.interview.application

import com.yourssu.scouter.auth.support.annotation.AuthUser
import com.yourssu.scouter.auth.support.resolver.AuthUserInfo
import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentDto
import com.yourssu.scouter.recruiting.interview.application.dto.CreateInterviewCommentRequest
import com.yourssu.scouter.recruiting.interview.application.dto.ReadInterviewCommentResponse
import com.yourssu.scouter.recruiting.interview.business.InterviewCommentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@Tag(name = "면접 질문 코멘트")
@RestController
class InterviewMemoController(
    private val interviewCommentService: InterviewCommentService,
) {

    @Operation(summary = "면접 질문 메모 목록 조회")
    @GetMapping("/applicants/{applicantId}/interviews/memos")
    fun read(
        @PathVariable applicantId: Long,
    ): ResponseEntity<List<ReadInterviewCommentResponse>> {
        val comments = interviewCommentService.readAllByApplicantId(applicantId)

        return ResponseEntity.ok(comments.map(ReadInterviewCommentResponse::from))
    }

    @Operation(summary = "면접 질문 메모 작성")
    @ApiResponse(
        description = "CREATED", responseCode = "201", headers = [
            Header(name = "Location", description = "/applicants/{applicantId}/interviews/memos/{commentId}"),
        ],
    )
    @PostMapping("/applicants/{applicantId}/interviews/memos")
    fun create(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
        @RequestBody @Valid request: CreateInterviewCommentRequest,
    ): ResponseEntity<Unit> {
        val dto: DocumentCommentDto = interviewCommentService.create(request.toCommand(applicantId, authUserInfo.userId))

        return ResponseEntity.created(URI.create("/applicants/$applicantId/interviews/memos/${dto.commentId}")).build()
    }

    @Operation(summary = "면접 질문 코멘트 삭제", description = "본인이 작성한 코멘트만 삭제 가능합니다.")
    @DeleteMapping("/applicants/{applicantId}/interviews/memos/{commentId}")
    fun deleteById(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
        @PathVariable commentId: Long,
    ): ResponseEntity<Unit> {
        interviewCommentService.deleteById(commentId, authUserInfo.userId)

        return ResponseEntity.ok().build()
    }
}

package com.yourssu.scouter.recruiting.comment.application

import com.yourssu.scouter.recruiting.comment.application.dto.CreateDocumentCommentRequest

import com.yourssu.scouter.recruiting.comment.application.dto.ReadDocumentCommentResponse

import com.yourssu.scouter.recruiting.comment.application.dto.UpdateDocumentCommentRequest

import com.yourssu.scouter.auth.support.application.authentication.AuthUser
import com.yourssu.scouter.auth.support.application.authentication.AuthUserInfo
import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentDto
import com.yourssu.scouter.recruiting.comment.business.DocumentCommentService
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

@Tag(name = "서류 코멘트")
@RestController
class DocumentCommentController(
    private val documentCommentService: DocumentCommentService,
) {

    @Operation(summary = "인라인 코멘트 조회")
    @GetMapping("/applicants/{applicantId}/documents/comments")
    fun readAllByApplicantId(
        @PathVariable applicantId: Long,
    ): ResponseEntity<List<ReadDocumentCommentResponse>> {
        val comments = documentCommentService.readAllByApplicantId(applicantId)

        return ResponseEntity.ok(comments.map(ReadDocumentCommentResponse::from))
    }

    @Operation(summary = "인라인 코멘트 작성")
    @ApiResponse(
        description = "CREATED", responseCode = "201", headers = [
            Header(name = "Location", description = "/applicants/{applicantId}/documents/comments/{commentId}"),
        ],
    )
    @PostMapping("/applicants/{applicantId}/documents/comments")
    fun create(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
        @RequestBody @Valid request: CreateDocumentCommentRequest,
    ): ResponseEntity<Unit> {
        val dto: DocumentCommentDto = documentCommentService.create(request.toCommand(applicantId, authUserInfo.userId))

        return ResponseEntity.created(URI.create("/applicants/$applicantId/documents/comments/${dto.commentId}")).build()
    }

    @Operation(summary = "인라인 코멘트 수정", description = "본인이 작성한 코멘트만 수정 가능합니다.")
    @PatchMapping("/applicants/{applicantId}/documents/comments/{commentId}")
    fun update(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
        @PathVariable commentId: Long,
        @RequestBody @Valid request: UpdateDocumentCommentRequest,
    ): ResponseEntity<ReadDocumentCommentResponse> {
        val dto = documentCommentService.update(request.toCommand(commentId, authUserInfo.userId))

        return ResponseEntity.ok(ReadDocumentCommentResponse.from(dto))
    }

    @Operation(summary = "인라인 코멘트 삭제", description = "본인이 작성한 코멘트만 삭제 가능합니다.")
    @DeleteMapping("/applicants/{applicantId}/documents/comments/{commentId}")
    fun deleteById(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
        @PathVariable commentId: Long,
    ): ResponseEntity<Unit> {
        documentCommentService.deleteById(commentId, authUserInfo.userId)

        return ResponseEntity.ok().build()
    }
}

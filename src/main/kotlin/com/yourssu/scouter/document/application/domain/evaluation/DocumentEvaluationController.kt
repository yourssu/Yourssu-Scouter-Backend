package com.yourssu.scouter.document.application.domain.evaluation

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantDto
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantPrivacyService
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantService
import com.yourssu.scouter.ats.business.support.exception.ApplicantAccessDeniedException
import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.document.business.domain.evaluation.DocumentEvaluationService
import com.yourssu.scouter.document.business.domain.evaluation.DocumentEvaluationViewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "서류 평가")
@RestController
class DocumentEvaluationController(
    private val documentEvaluationService: DocumentEvaluationService,
    private val documentEvaluationViewService: DocumentEvaluationViewService,
    private val applicantService: ApplicantService,
    private val applicantPrivacyService: ApplicantPrivacyService,
) {

    @Operation(summary = "서류 평가 조회 (본인)")
    @GetMapping("/applicants/{applicantId}/documents/evaluations")
    fun readMy(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
    ): ResponseEntity<ReadDocumentEvaluationResponse> {
        requireAccessible(applicantId, authUserInfo.userId)

        return ResponseEntity.ok(ReadDocumentEvaluationResponse.from(documentEvaluationService.readMy(applicantId, authUserInfo.userId)))
    }

    @Operation(summary = "서류 평가 저장/수정")
    @PutMapping("/applicants/{applicantId}/documents/evaluations")
    fun save(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
        @RequestBody @Valid request: SaveDocumentEvaluationRequest,
    ): ResponseEntity<Unit> {
        requireAccessible(applicantId, authUserInfo.userId)
        documentEvaluationService.save(request.toCommand(applicantId, authUserInfo.userId))

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "다른 평가자 서류 평가 조회")
    @GetMapping("/applicants/{applicantId}/documents/evaluations/others")
    fun readOthers(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
    ): ResponseEntity<List<ReadOtherDocumentEvaluationResponse>> {
        requireAccessible(applicantId, authUserInfo.userId)
        val others = documentEvaluationService.readOthers(applicantId, authUserInfo.userId)

        return ResponseEntity.ok(others.map(ReadOtherDocumentEvaluationResponse::from))
    }

    @Operation(summary = "서류 평가자 상태 목록 조회")
    @GetMapping("/applicants/{applicantId}/documents/evaluations/status")
    fun readStatuses(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
    ): ResponseEntity<List<ReadEvaluatorStatusResponse>> {
        requireAccessible(applicantId, authUserInfo.userId)
        val statuses = documentEvaluationService.readStatuses(applicantId)

        return ResponseEntity.ok(statuses.map(ReadEvaluatorStatusResponse::from))
    }

    @Operation(summary = "서류 평가 화면 통합 조회")
    @GetMapping("/applicants/{applicantId}/documents/evaluation-view")
    fun readEvaluationView(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
    ): ResponseEntity<ReadEvaluationViewResponse> {
        requireAccessible(applicantId, authUserInfo.userId)
        val view = documentEvaluationViewService.read(applicantId, authUserInfo.userId)

        return ResponseEntity.ok(ReadEvaluationViewResponse.from(view))
    }

    private fun requireAccessible(applicantId: Long, userId: Long) {
        val applicantDto: ApplicantDto = applicantService.readById(applicantId)
        val accessible = applicantPrivacyService.filterAccessibleApplicants(userId, listOf(applicantDto))
        if (accessible.isEmpty()) {
            throw ApplicantAccessDeniedException("해당 지원자의 정보를 조회할 권한이 없습니다.")
        }
    }
}

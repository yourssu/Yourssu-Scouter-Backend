package com.yourssu.scouter.recruiting.evaluation.application

import com.yourssu.scouter.recruiting.evaluation.application.dto.ReadDocumentEvaluationResponse

import com.yourssu.scouter.recruiting.evaluation.application.dto.ReadEvaluationViewResponse

import com.yourssu.scouter.recruiting.evaluation.application.dto.ReadOtherDocumentEvaluationResponse

import com.yourssu.scouter.recruiting.evaluation.application.dto.SaveDocumentEvaluationRequest

import com.yourssu.scouter.recruiting.evaluation.application.dto.ReadEvaluatorStatusResponse

import com.yourssu.scouter.recruiting.applicant.business.ApplicantAccessScope
import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantDto
import com.yourssu.scouter.recruiting.applicant.business.ApplicantPrivacyService
import com.yourssu.scouter.recruiting.applicant.business.ApplicantService
import com.yourssu.scouter.recruiting.support.business.exception.ApplicantAccessDeniedException
import com.yourssu.scouter.auth.support.application.authentication.AuthUser
import com.yourssu.scouter.auth.support.application.authentication.AuthUserInfo
import com.yourssu.scouter.recruiting.evaluation.business.DocumentEvaluationService
import com.yourssu.scouter.recruiting.evaluation.business.DocumentEvaluationViewService
import com.yourssu.scouter.hrms.member.business.MemberPrivacyService
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
    private val memberPrivacyService: MemberPrivacyService,
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
        val isPrivileged = memberPrivacyService.isPrivilegedUser(userId)
        val memberPartIds = if (isPrivileged) emptySet() else memberPrivacyService.getMemberPartIds(userId)
        val scope = ApplicantAccessScope(isPrivileged, memberPartIds)
        val accessible = applicantPrivacyService.filterAccessibleApplicants(scope, listOf(applicantDto))
        if (accessible.isEmpty()) {
            throw ApplicantAccessDeniedException("해당 지원자의 정보를 조회할 권한이 없습니다.")
        }
    }
}

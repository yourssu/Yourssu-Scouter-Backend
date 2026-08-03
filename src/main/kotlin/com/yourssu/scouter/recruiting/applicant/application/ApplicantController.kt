package com.yourssu.scouter.recruiting.applicant.application

import com.yourssu.scouter.recruiting.applicant.application.dto.UpdateApplicantRequest
import com.yourssu.scouter.recruiting.applicant.application.dto.UpdateAssignmentResultRequest

import com.yourssu.scouter.recruiting.applicant.application.dto.ReadApplicantResponse

import com.yourssu.scouter.recruiting.applicant.application.dto.ReadApplicantAnswerResponse

import com.yourssu.scouter.recruiting.applicant.application.dto.CreateApplicantRequest

import com.yourssu.scouter.recruiting.applicant.business.ApplicantAccessScope
import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantDto
import com.yourssu.scouter.recruiting.applicant.business.ApplicantPrivacyService
import com.yourssu.scouter.recruiting.applicant.business.ApplicantService
import com.yourssu.scouter.recruiting.support.business.exception.ApplicantAccessDeniedException
import com.yourssu.scouter.common.support.application.exception.ExceptionResponse
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSort
import com.yourssu.scouter.auth.support.application.authentication.AuthUser
import com.yourssu.scouter.auth.support.application.authentication.AuthUserInfo
import com.yourssu.scouter.hrms.member.business.MemberPrivacyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@Tag(name = "리크루팅 지원자")
@RestController
class ApplicantController(
    private val applicantService: ApplicantService,
    private val applicantPrivacyService: ApplicantPrivacyService,
    private val memberPrivacyService: MemberPrivacyService,
) {

    private fun resolveAccessScope(userId: Long): ApplicantAccessScope {
        val isPrivileged = memberPrivacyService.isPrivilegedUser(userId)
        val memberPartIds = if (isPrivileged) emptySet() else memberPrivacyService.getMemberPartIds(userId)
        return ApplicantAccessScope(isPrivileged, memberPartIds)
    }

    @Operation(summary = "지원자 추가 API")
    @ApiResponse(
        description = "CREATED", responseCode = "201", headers = [
            Header(name = "Location", description = "/applicants/{applicantId}")
        ]
    )
    @PostMapping("/applicants")
    fun create(
        @RequestBody @Valid request: CreateApplicantRequest,
    ): ResponseEntity<Unit> {
        val command = request.toCommand()
        val applicantId: Long = applicantService.create(command)

        return ResponseEntity.created(URI.create("/applicants/$applicantId")).build()
    }


    @Operation(
        summary = "지원자 목록 조회",
        description = "지원자 목록을 검색, 필터링을 통해 얻습니다. 필터링에 필요한 정보는 각 api에서 얻어야 합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (states/sort 값이 허용되지 않는 값인 경우)",
            content = [
                Content(schema = Schema(implementation = ExceptionResponse::class)),
            ],
        ),
        ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 semesterId 또는 partId를 조회한 경우",
            content = [
                Content(schema = Schema(implementation = ExceptionResponse::class)),
            ],
        ),
    )
    @GetMapping("/applicants")
    fun readAll(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) states: List<String>?,
        @RequestParam(required = false) semesterId: Long?,
        @RequestParam(required = false) partId: Long?,
        @RequestParam(required = false, defaultValue = "DEFAULT") sort: String = "DEFAULT",
    ): ResponseEntity<List<ReadApplicantResponse>> {
        val applicantSort = runCatching { ApplicantSort.valueOf(sort) }
            .getOrElse { throw IllegalArgumentException("허용되지 않는 sort 값입니다: $sort") }
        val applicantDtos: List<ApplicantDto> = applicantService.readAllByFilters(
            name = name,
            states = states,
            semesterId = semesterId,
            partId = partId,
            sort = applicantSort,
        )
        val accessible = applicantPrivacyService.filterAccessibleApplicants(resolveAccessScope(authUserInfo.userId), applicantDtos)
        val responses = accessible.map(ReadApplicantResponse::from)
        return ResponseEntity.ok(responses)
    }

    @Operation(summary = "지원자 정보 수정", description = "변경할 지원자 정보의 값을 보냅니다. 변경되지 않은 값은 보내면 안 됩니다.")
    @PatchMapping("/applicants/{applicantId}")
    fun updateById(
        @PathVariable applicantId: Long,
        @RequestBody @Valid request: UpdateApplicantRequest,
    ): ResponseEntity<Unit> {
        val command = request.toCommand(applicantId)
        applicantService.updateById(command)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "지원자 단일 조회", description = "지원자 목록 조회에서 얻은 applicantId를 이용해 지원자의 세부정보를 확인합니다.")
    @GetMapping("/applicants/{applicantId}")
    fun readById(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
    ): ResponseEntity<ReadApplicantResponse> {
        val applicantDto: ApplicantDto = applicantService.readById(applicantId)
        val accessible = applicantPrivacyService.filterAccessibleApplicants(resolveAccessScope(authUserInfo.userId), listOf(applicantDto))
        if (accessible.isEmpty()) {
            throw ApplicantAccessDeniedException("해당 지원자의 정보를 조회할 권한이 없습니다.")
        }
        return ResponseEntity.ok(ReadApplicantResponse.from(accessible.first()))
    }

    @Operation(
        summary = "지원자 서류 응답 조회",
        description = "지원자 필드로 매핑되지 않고 저장된 폼의 질문/응답 목록을 조회합니다."
    )
    @GetMapping("/applicants/{applicantId}/answers")
    fun readAnswersById(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable applicantId: Long,
    ): ResponseEntity<List<ReadApplicantAnswerResponse>> {
        val applicantDto: ApplicantDto = applicantService.readById(applicantId)
        val accessible = applicantPrivacyService.filterAccessibleApplicants(resolveAccessScope(authUserInfo.userId), listOf(applicantDto))
        if (accessible.isEmpty()) {
            throw ApplicantAccessDeniedException("해당 지원자의 정보를 조회할 권한이 없습니다.")
        }

        val answers = applicantService.readAnswersByApplicantId(applicantId)
        return ResponseEntity.ok(answers.map(ReadApplicantAnswerResponse::from))
    }

    @Operation(summary = "지원자별 과제 평가", description = "지원자별 합격 여부(PASSED / FAILED)를 설정합니다.")
    @PatchMapping("/applicants/{applicantId}/assignments")
    fun updateAssignmentResult(
        @PathVariable applicantId: Long,
        @RequestBody @Valid request: UpdateAssignmentResultRequest,
    ): ResponseEntity<Unit> {
        applicantService.updateAssignmentResult(request.toCommand(applicantId))
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "지원자 삭제", description = "지원자 목록 조회에서 얻은 applicantId를 이용해 지원자를 삭제합니다.")
    @ApiResponse(description = "NO_CONTENT", responseCode = "204")
    @DeleteMapping("/applicants/{applicantId}")
    fun deleteById(
        @PathVariable applicantId: Long,
    ): ResponseEntity<Unit> {
        applicantService.deleteById(applicantId)

        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "지원자 상태 목록 조회", description = "전체 지원자의 가능한 상태 목록을 확인합니다. 현재 지원자와는 관련이 없습니다.")
    @ApiResponse(
        description = "OK", responseCode = "200", content = [Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = String::class)),
            examples =
                [ExampleObject(value = "[ \"UNDER_REVIEW\", \"DOCUMENT_ACCEPTED\", \"DOCUMENT_REJECTED\", \"INTERVIEW_ACCEPTED\", \"INTERVIEW_REJECTED\", \"INCUBATING_REJECTED\", \"FINAL_ACCEPTED\" ]")]
        )]
    )
    @GetMapping("/applicants/states")
    fun readAllMemberStates(): ResponseEntity<List<String>> {
        val states: List<String> = applicantService.readAllStates()

        return ResponseEntity.ok(states)
    }
}

package com.yourssu.scouter.recruiting.evaluation.application

import com.yourssu.scouter.auth.support.application.authentication.AuthUser
import com.yourssu.scouter.auth.support.application.authentication.AuthUserInfo
import com.yourssu.scouter.hrms.member.business.MemberPrivacyService
import com.yourssu.scouter.recruiting.applicant.business.ApplicantAccessScope
import com.yourssu.scouter.recruiting.applicant.business.ApplicantPrivacyService
import com.yourssu.scouter.recruiting.applicant.business.ApplicantService
import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantDto
import com.yourssu.scouter.recruiting.evaluation.application.dto.ReadInterviewEvaluationResponse
import com.yourssu.scouter.recruiting.evaluation.application.dto.ReadOtherInterviewEvaluationResponse
import com.yourssu.scouter.recruiting.evaluation.application.dto.ReadEvaluatorStatusResponse
import com.yourssu.scouter.recruiting.evaluation.application.dto.SaveInterviewEvaluationRequest
import com.yourssu.scouter.recruiting.evaluation.business.InterviewEvaluationService
import com.yourssu.scouter.recruiting.support.business.exception.ApplicantAccessDeniedException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "면접 평가", description = "지원자별 면접 평가 내역 및 평가 상태 관리 API")
@RestController
class InterviewEvaluationController(
    private val interviewEvaluationService: InterviewEvaluationService,
    private val applicantService: ApplicantService,
    private val applicantPrivacyService: ApplicantPrivacyService,
    private val memberPrivacyService: MemberPrivacyService,
) {

    @Operation(summary = "면접 평가 조회 (본인)", description = "로그인한 면접관 본인의 임시저장/최종제출 면접 평가 내역을 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공", content = [Content(schema = Schema(implementation = ReadInterviewEvaluationResponse::class))]),
        ApiResponse(responseCode = "403", description = "해당 지원자의 정보를 조회할 권한이 없음"),
        ApiResponse(responseCode = "404", description = "지원자를 찾을 수 없음"),
    ])
    @GetMapping("/applicants/{applicantId}/interviews/evaluations")
    fun readMy(
        @AuthUser authUserInfo: AuthUserInfo,
        @Parameter(description = "지원자 ID", example = "1") @PathVariable applicantId: Long,
    ): ResponseEntity<ReadInterviewEvaluationResponse> {
        requireAccessible(applicantId, authUserInfo.userId)

        val myEvaluation = interviewEvaluationService.readMy(applicantId, authUserInfo.userId)
        return ResponseEntity.ok(ReadInterviewEvaluationResponse.from(myEvaluation))
    }

    @Operation(
        summary = "면접 평가 저장/수정",
        description = "로그인한 면접관 본인의 평가 정보를 저장합니다. submit이 false이면 임시저장, true이면 최종 제출입니다. 제출 시 요청된 점수가 배점을 초과하면 안 됩니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "평가 정보 저장 요청 바디",
            content = [Content(
                schema = Schema(implementation = SaveInterviewEvaluationRequest::class),
                examples = [ExampleObject(value = """{
  "items": [
    { "itemId": 1, "score": 8 }
  ],
  "overallComment": "질문에 대한 답변은 구조적이고 문화 적합성은 높습니다.",
  "result": "FINAL_PASS",
  "submit": true
}""")],
            )],
        ),
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "저장/수정 성공"),
        ApiResponse(responseCode = "400", description = "요청 형식이 올바르지 않거나 배점 한도를 초과함"),
        ApiResponse(responseCode = "403", description = "해당 지원자의 정보를 수정할 권한이 없음"),
        ApiResponse(responseCode = "404", description = "지원자 혹은 평가 항목을 찾을 수 없음"),
    ])
    @PutMapping("/applicants/{applicantId}/interviews/evaluations")
    fun save(
        @AuthUser authUserInfo: AuthUserInfo,
        @Parameter(description = "지원자 ID", example = "1") @PathVariable applicantId: Long,
        @RequestBody @Valid request: SaveInterviewEvaluationRequest,
    ): ResponseEntity<Unit> {
        requireAccessible(applicantId, authUserInfo.userId)

        interviewEvaluationService.save(request.toCommand(applicantId, authUserInfo.userId))
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "다른 평가자 면접 평가 조회", description = "다른 면접관들의 평가 내역을 조회합니다. 본인이 평가를 완료(최종 제출)한 경우에만 타인 평가가 조회 가능합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공", content = [Content(schema = Schema(implementation = ReadOtherInterviewEvaluationResponse::class))]),
        ApiResponse(responseCode = "403", description = "해당 지원자의 정보를 조회할 권한이 없거나, 본인이 아직 최종 제출하지 않아 블라인드 처리됨"),
        ApiResponse(responseCode = "404", description = "지원자를 찾을 수 없음"),
    ])
    @GetMapping("/applicants/{applicantId}/interviews/evaluations/others")
    fun readOthers(
        @AuthUser authUserInfo: AuthUserInfo,
        @Parameter(description = "지원자 ID", example = "1") @PathVariable applicantId: Long,
    ): ResponseEntity<List<ReadOtherInterviewEvaluationResponse>> {
        requireAccessible(applicantId, authUserInfo.userId)

        val others = interviewEvaluationService.readOthers(applicantId, authUserInfo.userId)
        return ResponseEntity.ok(others.map(ReadOtherInterviewEvaluationResponse::from))
    }

    @Operation(summary = "면접 평가자 상태 목록 조회", description = "해당 지원자에게 할당된 면접관들의 평가 작성 진행 상태를 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공", content = [Content(schema = Schema(implementation = ReadEvaluatorStatusResponse::class))]),
        ApiResponse(responseCode = "403", description = "해당 지원자의 정보를 조회할 권한이 없음"),
        ApiResponse(responseCode = "404", description = "지원자를 찾을 수 없음"),
    ])
    @GetMapping("/applicants/{applicantId}/interviews/evaluations/status")
    fun readStatuses(
        @AuthUser authUserInfo: AuthUserInfo,
        @Parameter(description = "지원자 ID", example = "1") @PathVariable applicantId: Long,
    ): ResponseEntity<List<ReadEvaluatorStatusResponse>> {
        requireAccessible(applicantId, authUserInfo.userId)

        val statuses = interviewEvaluationService.readStatuses(applicantId)
        return ResponseEntity.ok(statuses.map(ReadEvaluatorStatusResponse::from))
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

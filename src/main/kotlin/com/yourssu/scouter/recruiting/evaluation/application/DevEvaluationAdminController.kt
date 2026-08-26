package com.yourssu.scouter.recruiting.evaluation.application

import com.yourssu.scouter.auth.support.annotation.AuthUser
import com.yourssu.scouter.auth.support.resolver.AuthUserInfo
import com.yourssu.scouter.member.core.business.MemberPrivacyService
import com.yourssu.scouter.member.support.exception.MemberAccessDeniedException
import com.yourssu.scouter.recruiting.evaluation.business.DocumentEvaluationService
import com.yourssu.scouter.recruiting.evaluation.business.InterviewEvaluationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * local, dev 프로필 전용. QA를 위해 지원자의 서류/면접 평가 데이터를 임의로 초기화할 수 있게 함.
 * 운영(prod) 환경에는 이 컨트롤러가 아예 등록되지 않는다.
 */
@Profile("local", "dev")
@Tag(name = "[DEV] 지원자 평가 데이터 삭제")
@RestController
@RequestMapping("/internal/dev/applicants/{applicantId}/evaluations")
class DevEvaluationAdminController(
    private val documentEvaluationService: DocumentEvaluationService,
    private val interviewEvaluationService: InterviewEvaluationService,
    private val memberPrivacyService: MemberPrivacyService,
) {

    @Operation(summary = "서류 평가 데이터 삭제 (QA용)", description = "해당 지원자의 서류 평가 데이터를 삭제합니다. 스카우터 팀원만 호출 가능.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "403", description = "스카우터 팀원이 아님"),
    )
    @DeleteMapping("/documents")
    fun deleteDocumentEvaluations(
        @AuthUser authUserInfo: AuthUserInfo,
        @Parameter(description = "지원자 ID", example = "1") @PathVariable applicantId: Long,
    ): ResponseEntity<Unit> {
        requireScouterTeamMember(authUserInfo.userId)
        documentEvaluationService.deleteAllByApplicantId(applicantId)

        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "면접 평가 데이터 삭제 (QA용)", description = "해당 지원자의 면접 평가 및 최종 평가 데이터를 삭제합니다. 스카우터 팀원만 호출 가능.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "403", description = "스카우터 팀원이 아님"),
    )
    @DeleteMapping("/interviews")
    fun deleteInterviewEvaluations(
        @AuthUser authUserInfo: AuthUserInfo,
        @Parameter(description = "지원자 ID", example = "1") @PathVariable applicantId: Long,
    ): ResponseEntity<Unit> {
        requireScouterTeamMember(authUserInfo.userId)
        interviewEvaluationService.deleteAllByApplicantId(applicantId)

        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "서류/면접 평가 데이터 전체 삭제 (QA용)", description = "해당 지원자의 서류 평가, 면접 평가, 최종 평가 데이터를 모두 삭제합니다. 스카우터 팀원만 호출 가능.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "403", description = "스카우터 팀원이 아님"),
    )
    @DeleteMapping
    fun deleteAllEvaluations(
        @AuthUser authUserInfo: AuthUserInfo,
        @Parameter(description = "지원자 ID", example = "1") @PathVariable applicantId: Long,
    ): ResponseEntity<Unit> {
        requireScouterTeamMember(authUserInfo.userId)
        documentEvaluationService.deleteAllByApplicantId(applicantId)
        interviewEvaluationService.deleteAllByApplicantId(applicantId)

        return ResponseEntity.noContent().build()
    }

    private fun requireScouterTeamMember(userId: Long) {
        if (!memberPrivacyService.isScouterTeamMember(userId)) {
            throw MemberAccessDeniedException("이 API는 스카우터 팀원만 사용할 수 있습니다.")
        }
    }
}

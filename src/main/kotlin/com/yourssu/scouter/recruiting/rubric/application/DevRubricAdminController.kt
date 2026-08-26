package com.yourssu.scouter.recruiting.rubric.application

import com.yourssu.scouter.auth.support.annotation.AuthUser
import com.yourssu.scouter.auth.support.resolver.AuthUserInfo
import com.yourssu.scouter.member.core.business.MemberPrivacyService
import com.yourssu.scouter.member.support.exception.MemberAccessDeniedException
import com.yourssu.scouter.recruiting.rubric.business.InterviewRubricService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

/**
 * local, dev 프로필 전용. QA를 위해 파트/학기별 면접 루브릭을 초기화할 수 있게 함.
 * 운영(prod) 환경에는 이 컨트롤러가 아예 등록되지 않는다.
 */
@Profile("local", "dev")
@Tag(name = "[DEV] 면접 루브릭 초기화")
@RestController
@Validated
class DevRubricAdminController(
    private val interviewRubricService: InterviewRubricService,
    private val memberPrivacyService: MemberPrivacyService,
) {

    @Operation(
        summary = "면접 루브릭 초기화 (QA용)",
        description = "해당 파트·학기의 면접 루브릭을 삭제합니다. 삭제 후에는 면접 요구조건 기반의 미저장 템플릿으로 다시 조회됩니다. " +
            "해당 루브릭 항목을 참조하는 면접 평가가 남아있으면 초기화할 수 없습니다. 스카우터 팀원만 호출 가능.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "403", description = "스카우터 팀원이 아님"),
        ApiResponse(responseCode = "409", description = "해당 파트에 면접 평가가 존재해 초기화할 수 없음"),
        ApiResponse(responseCode = "404", description = "파트를 찾을 수 없음"),
    )
    @DeleteMapping("/internal/dev/parts/{partId}/interviews/rubrics/{semester}")
    fun resetByPartIdAndSemester(
        @AuthUser authUserInfo: AuthUserInfo,
        @Parameter(description = "파트 ID", example = "3") @PathVariable partId: Long,
        @Parameter(description = "학기 식별자. YYYY-1 또는 YYYY-2 형식", example = "2026-1")
        @PathVariable @Pattern(regexp = "^\\d{4}-[12]$", message = "semester must use YYYY-1 or YYYY-2 format") semester: String,
    ): ResponseEntity<Unit> {
        requireScouterTeamMember(authUserInfo.userId)
        interviewRubricService.resetByPartIdAndSemester(partId, semester)

        return ResponseEntity.noContent().build()
    }

    private fun requireScouterTeamMember(userId: Long) {
        if (!memberPrivacyService.isScouterTeamMember(userId)) {
            throw MemberAccessDeniedException("이 API는 스카우터 팀원만 사용할 수 있습니다.")
        }
    }
}

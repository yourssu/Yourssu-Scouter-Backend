package com.yourssu.scouter.recruiting.rubric.application

import com.yourssu.scouter.auth.support.annotation.AuthUser
import com.yourssu.scouter.auth.support.resolver.AuthUserInfo
import com.yourssu.scouter.member.core.business.MemberPrivacyService
import com.yourssu.scouter.member.support.exception.MemberAccessDeniedException
import com.yourssu.scouter.recruiting.rubric.business.DocumentSectionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * local, dev 프로필 전용. QA를 위해 파트의 서류 평가 문항 배점을 일괄 0점으로 초기화할 수 있게 함.
 * 운영(prod) 환경에는 이 컨트롤러가 아예 등록되지 않는다.
 */
@Profile("local", "dev")
@Tag(name = "[DEV] 서류 평가 문항 배점 초기화")
@RestController
@RequestMapping("/internal/dev/parts/{partId}/documents/rubrics")
class DevDocumentRubricAdminController(
    private val documentSectionService: DocumentSectionService,
    private val memberPrivacyService: MemberPrivacyService,
) {

    @Operation(
        summary = "서류 평가 문항 배점 초기화 (QA용)",
        description = "해당 파트의 모든 서류 평가 문항 배점을 0으로 초기화합니다. " +
            "해당 문항을 참조하는 서류 평가가 남아있으면 초기화할 수 없습니다. 스카우터 팀원만 호출 가능.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "초기화 성공"),
        ApiResponse(responseCode = "403", description = "스카우터 팀원이 아님"),
        ApiResponse(responseCode = "409", description = "해당 파트에 서류 평가가 존재해 초기화할 수 없음"),
        ApiResponse(responseCode = "404", description = "파트를 찾을 수 없음"),
    )
    @PatchMapping("/reset-max-scores")
    fun resetMaxScoresToZero(
        @AuthUser authUserInfo: AuthUserInfo,
        @Parameter(description = "파트 ID", example = "1") @PathVariable partId: Long,
    ): ResponseEntity<Unit> {
        requireScouterTeamMember(authUserInfo.userId)
        documentSectionService.resetMaxScoresToZero(partId)

        return ResponseEntity.ok().build()
    }

    private fun requireScouterTeamMember(userId: Long) {
        if (!memberPrivacyService.isScouterTeamMember(userId)) {
            throw MemberAccessDeniedException("이 API는 스카우터 팀원만 사용할 수 있습니다.")
        }
    }
}

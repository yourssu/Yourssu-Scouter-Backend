package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.hrms.business.domain.member.MemberPrivacyService
import com.yourssu.scouter.hrms.business.support.DevPrivilegeTestHolder
import com.yourssu.scouter.hrms.business.support.exception.MemberAccessDeniedException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * local, dev 프로필 전용. 스카우터 팀원이 자기 자신의 민감정보 열람 권한을 일시적으로 끄고/켜서
 * 마스킹 동작을 Swagger 등으로 테스트할 수 있게 함.
 */
@Profile("local", "dev")
@Tag(name = "[DEV] 멤버 민감정보 권한 테스트")
@RestController
@RequestMapping("/internal/dev/member-privacy")
class DevMemberPrivacyAdminController(
    private val memberPrivacyService: MemberPrivacyService,
    private val devPrivilegeTestHolder: DevPrivilegeTestHolder,
) {

    @Operation(
        summary = "나를 비권한자로 전환 (테스트용)",
        description = "이후 멤버 API 호출 시 내 계정은 민감정보가 마스킹된 응답을 받습니다. 스카우터 팀원만 호출 가능.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "적용됨"),
        ApiResponse(responseCode = "403", description = "스카우터 팀원이 아님"),
    )
    @PostMapping("/privileged/self/disable")
    fun disablePrivilegeForSelf(@AuthUser authUserInfo: AuthUserInfo): ResponseEntity<Unit> {
        requireScouterTeamMember(authUserInfo.userId)
        devPrivilegeTestHolder.markAsNonPrivilegedForTest(authUserInfo.userId)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "나를 권한자로 복원 (테스트용)",
        description = "비권한자로 전환한 상태를 해제하고, 다시 민감정보 열람 권한이 있는 상태로 돌립니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "적용됨"),
        ApiResponse(responseCode = "403", description = "스카우터 팀원이 아님"),
    )
    @PostMapping("/privileged/self/enable")
    fun enablePrivilegeForSelf(@AuthUser authUserInfo: AuthUserInfo): ResponseEntity<Unit> {
        requireScouterTeamMember(authUserInfo.userId)
        devPrivilegeTestHolder.unmark(authUserInfo.userId)
        return ResponseEntity.noContent().build()
    }

    private fun requireScouterTeamMember(userId: Long) {
        if (!memberPrivacyService.isScouterTeamMember(userId)) {
            throw MemberAccessDeniedException("이 API는 스카우터 팀원만 사용할 수 있습니다.")
        }
    }
}

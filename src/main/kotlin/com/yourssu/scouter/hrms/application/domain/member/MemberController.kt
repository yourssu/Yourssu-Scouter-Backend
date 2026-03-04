package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.hrms.business.domain.member.ActiveMemberDto
import com.yourssu.scouter.hrms.business.domain.member.GraduatedMemberDto
import com.yourssu.scouter.hrms.business.domain.member.InactiveMemberDto
import com.yourssu.scouter.hrms.business.domain.member.MemberPrivacyService
import com.yourssu.scouter.hrms.business.domain.member.MemberService
import com.yourssu.scouter.hrms.business.domain.member.UpdateActiveMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateGraduatedMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateInactiveMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateWithdrawnMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.WithdrawnMemberDto
import com.yourssu.scouter.hrms.business.support.exception.MemberAccessDeniedException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "유어슈 멤버")
@RestController
class MemberController(
    private val memberService: MemberService,
    private val memberPrivacyService: MemberPrivacyService,
) {

    @Operation(summary = "액티브 멤버 목록 조회/검색")
    @GetMapping("/members/active")
    fun readAllActive(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) partId: Long?,
    ): ResponseEntity<MemberListResponse<ReadActiveMemberListItemResponse>> {
        val isPrivileged: Boolean = memberPrivacyService.isPrivilegedUser(authUserInfo.userId)
        val activeMemberDtos: List<ActiveMemberDto> = memberService.readAllActiveByFilters(
            search = search,
            partId = partId,
        )
        val items: List<ReadActiveMemberListItemResponse> =
            activeMemberDtos.map { ReadActiveMemberListItemResponse.from(it) }
        val finalItems: List<ReadActiveMemberListItemResponse> =
            if (isPrivileged) items else items.map(ReadActiveMemberListItemResponse::maskSensitive)
        return ResponseEntity.ok(MemberListResponse(members = finalItems, isSensitiveMasked = !isPrivileged))
    }

    @Operation(summary = "비액티브 멤버 목록 조회/검색")
    @GetMapping("/members/inactive")
    fun readAllInActive(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) partId: Long?,
    ): ResponseEntity<MemberListResponse<ReadInactiveMemberListItemResponse>> {
        val isPrivileged: Boolean = memberPrivacyService.isPrivilegedUser(authUserInfo.userId)
        val inactiveMemberDtos: List<InactiveMemberDto> = memberService.readAllInActiveByFilters(
            search = search,
            partId = partId,
        )
        val items: List<ReadInactiveMemberListItemResponse> =
            inactiveMemberDtos.map { ReadInactiveMemberListItemResponse.from(it) }
        val finalItems: List<ReadInactiveMemberListItemResponse> =
            if (isPrivileged) items else items.map(ReadInactiveMemberListItemResponse::maskSensitive)
        return ResponseEntity.ok(MemberListResponse(members = finalItems, isSensitiveMasked = !isPrivileged))
    }

    @Operation(summary = "졸업 멤버 목록 조회/검색")
    @GetMapping("/members/graduated")
    fun readAllGraduated(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) partId: Long?,
    ): ResponseEntity<MemberListResponse<ReadGraduatedMemberListItemResponse>> {
        val isPrivileged: Boolean = memberPrivacyService.isPrivilegedUser(authUserInfo.userId)
        val graduatedMemberDtos: List<GraduatedMemberDto> = memberService.readAllGraduatedByFilters(
            search = search,
            partId = partId,
        )
        val items: List<ReadGraduatedMemberListItemResponse> =
            graduatedMemberDtos.map { ReadGraduatedMemberListItemResponse.from(it) }
        val finalItems: List<ReadGraduatedMemberListItemResponse> =
            if (isPrivileged) items else items.map(ReadGraduatedMemberListItemResponse::maskSensitive)
        return ResponseEntity.ok(MemberListResponse(members = finalItems, isSensitiveMasked = !isPrivileged))
    }

    @Operation(summary = "탈퇴 멤버 목록 조회/검색")
    @GetMapping("/members/withdrawn")
    fun readAllWithdrawn(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) partId: Long?,
    ): ResponseEntity<MemberListResponse<ReadWithdrawnMemberListItemResponse>> {
        val isPrivileged: Boolean = memberPrivacyService.isPrivilegedUser(authUserInfo.userId)
        val withdrawnMemberDtos: List<WithdrawnMemberDto> = memberService.readAllWithdrawnByFilters(
            search = search,
            partId = partId,
        )
        val items: List<ReadWithdrawnMemberListItemResponse> =
            withdrawnMemberDtos.map { ReadWithdrawnMemberListItemResponse.from(it) }
        val finalItems: List<ReadWithdrawnMemberListItemResponse> =
            if (isPrivileged) items else items.map(ReadWithdrawnMemberListItemResponse::maskSensitive)
        return ResponseEntity.ok(MemberListResponse(members = finalItems, isSensitiveMasked = !isPrivileged))
    }

    @Operation(summary = "액티브 멤버 정보 수정", description = "변경되지 않은 정보는 보내면 안됩니다.")
    @PatchMapping("/members/active/{memberId}")
    fun updateActiveById(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable memberId: Long,
        @RequestBody @Valid request: UpdateActiveMemberRequest,
    ): ResponseEntity<Unit> {
        val isPrivileged: Boolean = memberPrivacyService.isPrivilegedUser(authUserInfo.userId)
        if (!isPrivileged) {
            throw MemberAccessDeniedException("멤버 정보를 수정할 권한이 없습니다.")
        }
        val command: UpdateActiveMemberCommand = request.toCommand(memberId)
        memberService.updateActiveById(command)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "비액티브 멤버 정보 수정", description = "변경되지 않은 정보는 보내면 안됩니다.")
    @PatchMapping("/members/inactive/{memberId}")
    fun updateInactiveById(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable memberId: Long,
        @RequestBody @Valid request: UpdateInactiveMemberRequest,
    ): ResponseEntity<Unit> {
        val isPrivileged: Boolean = memberPrivacyService.isPrivilegedUser(authUserInfo.userId)
        if (!isPrivileged) {
            throw MemberAccessDeniedException("멤버 정보를 수정할 권한이 없습니다.")
        }
        val command: UpdateInactiveMemberCommand = request.toCommand(memberId)
        memberService.updateInactiveById(command)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "졸업 멤버 정보 수정", description = "변경되지 않은 정보는 보내면 안됩니다.")
    @PatchMapping("/members/graduated/{memberId}")
    fun updateGraduatedById(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable memberId: Long,
        @RequestBody @Valid request: UpdateGraduatedMemberRequest,
    ): ResponseEntity<Unit> {
        val isPrivileged: Boolean = memberPrivacyService.isPrivilegedUser(authUserInfo.userId)
        if (!isPrivileged) {
            throw MemberAccessDeniedException("멤버 정보를 수정할 권한이 없습니다.")
        }
        val command: UpdateGraduatedMemberCommand = request.toCommand(memberId)
        memberService.updateGraduatedById(command)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "탈퇴 멤버 정보 수정", description = "변경되지 않은 정보는 보내면 안됩니다.")
    @PatchMapping("/members/withdrawn/{memberId}")
    fun updateWithdrawnById(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable memberId: Long,
        @RequestBody @Valid request: UpdateWithdrawnMemberRequest,
    ): ResponseEntity<Unit> {
        val isPrivileged: Boolean = memberPrivacyService.isPrivilegedUser(authUserInfo.userId)
        if (!isPrivileged) {
            throw MemberAccessDeniedException("멤버 정보를 수정할 권한이 없습니다.")
        }
        val command: UpdateWithdrawnMemberCommand = request.toCommand(memberId)
        memberService.updateWithdrawnById(command)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "멤버 역할 목록 조회", description = "Lead, Vice Lead, Member 등 역할을 조회합니다.")
    @GetMapping("/members/roles")
    fun readAllMemberRoles(): ResponseEntity<List<String>> {
        val roles: List<String> = memberService.readAllRoles()

        return ResponseEntity.ok(roles)
    }

    @Operation(summary = "멤버 상태 목록 조회", description = "액티브, 비액티브, 수료, 졸업, 탈퇴 등 상태를 조회합니다.")
    @GetMapping("/members/states")
    fun readAllMemberStates(): ResponseEntity<List<String>> {
        val states: List<String> = memberService.readAllStates()

        return ResponseEntity.ok(states)
    }
}

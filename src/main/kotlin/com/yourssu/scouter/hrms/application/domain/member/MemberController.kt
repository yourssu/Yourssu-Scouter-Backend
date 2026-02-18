package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.ActiveMemberDto
import com.yourssu.scouter.hrms.business.domain.member.GraduatedMemberDto
import com.yourssu.scouter.hrms.business.domain.member.InactiveMemberDto
import com.yourssu.scouter.hrms.business.domain.member.MemberService
import com.yourssu.scouter.hrms.business.domain.member.UpdateActiveMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateGraduatedMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateInactiveMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.UpdateWithdrawnMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.WithdrawnMemberDto
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
) {

    @Operation(summary = "액티브 멤버 목록 조회/검색")
    @GetMapping("/members/active")
    fun readAllActive(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) partId: Long?,
    ): ResponseEntity<List<ReadActiveMemberResponse>> {
        val activeMemberDtos: List<ActiveMemberDto> = memberService.readAllActiveByFilters(
            search = search,
            partId = partId,
        )
        val responses: List<ReadActiveMemberResponse> = activeMemberDtos.map { ReadActiveMemberResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @Operation(summary = "비액티브 멤버 목록 조회/검색")
    @GetMapping("/members/inactive")
    fun readAllInActive(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) partId: Long?,
    ): ResponseEntity<List<ReadInactiveMemberResponse>> {
        val inactiveMemberDtos: List<InactiveMemberDto> = memberService.readAllInActiveByFilters(
            search = search,
            partId = partId,
        )
        val responses: List<ReadInactiveMemberResponse> = inactiveMemberDtos.map { ReadInactiveMemberResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @Operation(summary = "졸업 멤버 목록 조회/검색")
    @GetMapping("/members/graduated")
    fun readAllGraduated(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) partId: Long?,
    ): ResponseEntity<List<ReadGraduatedMemberResponse>> {
        val graduatedMemberDtos: List<GraduatedMemberDto> = memberService.readAllGraduatedByFilters(
            search = search,
            partId = partId,
        )
        val responses: List<ReadGraduatedMemberResponse> =
            graduatedMemberDtos.map { ReadGraduatedMemberResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @Operation(summary = "탈퇴 멤버 목록 조회/검색")
    @GetMapping("members/withdrawn")
    fun readAllWithdrawn(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) partId: Long?,
    ): ResponseEntity<List<ReadWithdrawnMemberResponse>> {
        val withdrawnMemberDtos: List<WithdrawnMemberDto> = memberService.readAllWithdrawnByFilters(
            search = search,
            partId = partId,
        )
        val responses: List<ReadWithdrawnMemberResponse> =
            withdrawnMemberDtos.map { ReadWithdrawnMemberResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @Operation(summary = "액티브 멤버 정보 수정", description = "변경되지 않은 정보는 보내면 안됩니다.")
    @PatchMapping("/members/active/{memberId}")
    fun updateActiveById(
        @PathVariable memberId: Long,
        @RequestBody @Valid request: UpdateActiveMemberRequest,
    ): ResponseEntity<Unit> {
        val command: UpdateActiveMemberCommand = request.toCommand(memberId)
        memberService.updateActiveById(command)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "비액티브 멤버 정보 수정", description = "변경되지 않은 정보는 보내면 안됩니다.")
    @PatchMapping("/members/inactive/{memberId}")
    fun updateInactiveById(
        @PathVariable memberId: Long,
        @RequestBody @Valid request: UpdateInactiveMemberRequest,
    ): ResponseEntity<Unit> {
        val command: UpdateInactiveMemberCommand = request.toCommand(memberId)
        memberService.updateInactiveById(command)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "졸업 멤버 정보 수정", description = "변경되지 않은 정보는 보내면 안됩니다.")
    @PatchMapping("/members/graduated/{memberId}")
    fun updateGraduatedById(
        @PathVariable memberId: Long,
        @RequestBody @Valid request: UpdateGraduatedMemberRequest,
    ): ResponseEntity<Unit> {
        val command: UpdateGraduatedMemberCommand = request.toCommand(memberId)
        memberService.updateGraduatedById(command)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "탈퇴 멤버 정보 수정", description = "변경되지 않은 정보는 보내면 안됩니다.")
    @PatchMapping("/members/withdrawn/{memberId}")
    fun updateWithdrawnById(
        @PathVariable memberId: Long,
        @RequestBody @Valid request: UpdateWithdrawnMemberRequest,
    ): ResponseEntity<Unit> {
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

    @Operation(summary = "멤버 상태 목록 조회", description = "액티브, 비액티브, 졸업, 탈퇴 등 상태를 조회합니다.")
    @GetMapping("/members/states")
    fun readAllMemberStates(): ResponseEntity<List<String>> {
        val states: List<String> = memberService.readAllStates()

        return ResponseEntity.ok(states)
    }
}

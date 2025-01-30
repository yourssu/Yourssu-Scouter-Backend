package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.ActiveMemberDto
import com.yourssu.scouter.hrms.business.domain.member.GraduatedMemberDto
import com.yourssu.scouter.hrms.business.domain.member.InactiveMemberDto
import com.yourssu.scouter.hrms.business.domain.member.MemberService
import com.yourssu.scouter.hrms.business.domain.member.WithdrawnMemberDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberController(
    private val memberService: MemberService,
) {

    @GetMapping("/members/active")
    fun readAllActive(
        @RequestParam(required = false) search: String?,
    ): ResponseEntity<List<ReadActiveMemberResponse>> {
        val activeMemberDtos: List<ActiveMemberDto> = when {
            !search.isNullOrEmpty() -> memberService.searchAllActiveByNameOrNickname(search)
            else -> memberService.readAllActive()
        }
        val responses: List<ReadActiveMemberResponse> = activeMemberDtos.map { ReadActiveMemberResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @GetMapping("/members/inactive")
    fun readAllInActive(
        @RequestParam(required = false) search: String?,
    ): ResponseEntity<List<ReadInactiveMemberResponse>> {
        val inactiveMemberDtos: List<InactiveMemberDto> = when {
            !search.isNullOrEmpty() -> memberService.searchAllInactiveByNameOrNickname(search)
            else -> memberService.readAllInactive()
        }
        val responses: List<ReadInactiveMemberResponse> = inactiveMemberDtos.map { ReadInactiveMemberResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @GetMapping("/members/graduated")
    fun readAllGraduated(
        @RequestParam(required = false) search: String?,
    ): ResponseEntity<List<ReadGraduatedMemberResponse>> {
        val graduatedMemberDtos: List<GraduatedMemberDto> = when {
            !search.isNullOrEmpty() -> memberService.searchAllGraduatedByNameOrNickname(search)
            else -> memberService.readAllGraduated()
        }
        val responses: List<ReadGraduatedMemberResponse> =
            graduatedMemberDtos.map { ReadGraduatedMemberResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @GetMapping("members/withdrawn")
    fun readAllWithdrawn(
        @RequestParam(required = false) search: String?,
    ): ResponseEntity<List<ReadWithdrawnMemberResponse>> {
        val withdrawnMemberDtos: List<WithdrawnMemberDto> = when {
            !search.isNullOrEmpty() -> memberService.searchAllWithdrawnByNameOrNickname(search)
            else -> memberService.readAllWithdrawn()
        }
        val responses: List<ReadWithdrawnMemberResponse> =
            withdrawnMemberDtos.map { ReadWithdrawnMemberResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @GetMapping("/members/roles")
    fun readAllMemberRoles(): ResponseEntity<List<String>> {
        val roles: List<String> = memberService.readAllRoles()

        return ResponseEntity.ok(roles)
    }

    @GetMapping("/members/states")
    fun readAllMemberStates(): ResponseEntity<List<String>> {
        val states: List<String> = memberService.readAllStates()

        return ResponseEntity.ok(states)
    }
}

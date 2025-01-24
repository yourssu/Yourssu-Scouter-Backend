package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.CreateMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.MemberDto
import com.yourssu.scouter.hrms.business.domain.member.MemberService
import com.yourssu.scouter.hrms.business.domain.member.UpdateMemberCommand
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberController(
    private val memberService: MemberService,
) {

    @PostMapping("/members")
    fun create(
        @RequestBody @Valid request: CreateMemberRequest,
    ): ResponseEntity<Unit> {
        val command: CreateMemberCommand = request.toCommand()
        val memberId: Long = memberService.create(command)

        return ResponseEntity.created(URI.create("/members/$memberId")).build()
    }

    @GetMapping("/members/{memberId}")
    fun readById(
        @PathVariable memberId: Long,
    ): ResponseEntity<ReadMemberResponse> {
        val memberDto: MemberDto = memberService.readById(memberId)
        val response = ReadMemberResponse.from(memberDto)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/members")
    fun readAll(): ResponseEntity<List<ReadMemberResponse>> {
        val memberDtos: List<MemberDto> = memberService.readAll()
        val responses: List<ReadMemberResponse> = memberDtos.map { ReadMemberResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @PatchMapping("/members/{memberId}")
    fun updateById(
        @PathVariable memberId: Long,
        @RequestBody @Valid request: UpdateMemberRequest,
    ): ResponseEntity<Unit> {
        val command: UpdateMemberCommand = request.toCommand(memberId)
        memberService.updateById(command)

        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/members/{memberId}")
    fun deleteById(
        @PathVariable memberId: Long,
    ): ResponseEntity<Unit> {
        memberService.deleteById(memberId)

        return ResponseEntity.noContent().build()
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

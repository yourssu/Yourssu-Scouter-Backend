package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.CreateMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.MemberDto
import com.yourssu.scouter.hrms.business.domain.member.MemberService
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberController(
    private val memberService: MemberService,
) {

    @PostMapping("/members")
    fun create(
        request: CreateMemberRequest,
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
}

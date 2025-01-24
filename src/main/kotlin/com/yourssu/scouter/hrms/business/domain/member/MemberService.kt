package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberReader: MemberReader,
) {

    fun readById(memberId: Long): MemberDto {
        val member: Member = memberReader.readById(memberId)

        return MemberDto.from(member)
    }

    fun readAll(): List<MemberDto> {
        val members: List<Member> = memberReader.readAll()

        return members.map { MemberDto.from(it) }
    }
}

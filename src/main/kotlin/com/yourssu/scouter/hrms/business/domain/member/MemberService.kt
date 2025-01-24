package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberWriter: MemberWriter,
    private val memberReader: MemberReader,
    private val departmentReader: DepartmentReader,
    private val partReader: PartReader,
) {

    fun create(command: CreateMemberCommand) {
        val department = departmentReader.readById(command.departmentId)
        val part = partReader.readById(command.partId)

        val member: Member = command.toDomain(department, part)

        memberWriter.write(member)
    }

    fun readById(memberId: Long): MemberDto {
        val member: Member = memberReader.readById(memberId)

        return MemberDto.from(member)
    }

    fun readAll(): List<MemberDto> {
        val members: List<Member> = memberReader.readAll()

        return members.map { MemberDto.from(it) }
    }
}

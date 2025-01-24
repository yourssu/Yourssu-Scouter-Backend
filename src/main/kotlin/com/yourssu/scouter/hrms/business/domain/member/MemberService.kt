package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.Part
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

    fun create(command: CreateMemberCommand): Long {
        val department: Department = departmentReader.readById(command.departmentId)
        val part: Part = partReader.readById(command.partId)
        val toWriteMember: Member = command.toDomain(department, part)
        val writtenMember: Member = memberWriter.write(toWriteMember)

        return writtenMember.id!!
    }

    fun readById(memberId: Long): MemberDto {
        val member: Member = memberReader.readById(memberId)

        return MemberDto.from(member)
    }

    fun readAll(): List<MemberDto> {
        val members: List<Member> = memberReader.readAll()

        return members.map { MemberDto.from(it) }
    }

    fun updateById(command: UpdateMemberCommand) {
        val target: Member = memberReader.readById(command.targetMemberId)
        val updated = Member(
            id = target.id,
            name = command.name?: target.name,
            email = command.email?: target.email,
            phoneNumber = command.phoneNumber?: target.phoneNumber,
            birthDate = command.birthDate?: target.birthDate,
            department = command.departmentId?.let { departmentReader.readById(it) }?: target.department,
            studentId = command.studentId?: target.studentId,
            part = command.partId?.let { partReader.readById(it) }?: target.part,
            role = command.role?: target.role,
            nicknameEnglish = command.nicknameEnglish?: target.nicknameEnglish,
            nicknameKorean = command.nicknameKorean?: target.nicknameKorean,
            state = command.state?: target.state,
            joinDate = command.joinDate?: target.joinDate,
            isMembershipFeePaid = command.membershipFee?: target.isMembershipFeePaid,
            note = command.note?: target.note,
        )

        memberWriter.write(updated)
    }

    fun deleteById(memberId: Long) {
        val target = memberReader.readById(memberId)

        memberWriter.delete(target)
    }
}

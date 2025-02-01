package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.implement.domain.member.ActiveMember
import com.yourssu.scouter.hrms.implement.domain.member.GraduatedMember
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMember
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.domain.member.WithdrawnMember
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberWriter: MemberWriter,
    private val memberReader: MemberReader,
    private val departmentReader: DepartmentReader,
    private val partReader: PartReader,
) {

    fun createMemberWithActiveState(command: CreateMemberCommand): Long {
        val department: Department = departmentReader.readById(command.departmentId)
        val parts: List<Part> = command.parts.map { partReader.readById(it) }
        val member: Member = command.toDomain(department, parts)
        val writtenActiveMember: ActiveMember = memberWriter.writeMemberWithActiveStatus(
            member = member,
            isMembershipFeePaid = false,
        )

        return writtenActiveMember.id!!
    }

    fun readAllActive(): List<ActiveMemberDto> {
        val members: List<ActiveMember> = memberReader.readAllActive()

        return members.map { ActiveMemberDto.from(it) }
    }

    fun readAllInactive(): List<InactiveMemberDto> {
        val members: List<InactiveMember> = memberReader.readAllInactive()

        return members.map { InactiveMemberDto.from(it) }
    }

    fun readAllGraduated(): List<GraduatedMemberDto> {
        val members: List<GraduatedMember> = memberReader.readAllGraduated()

        return members.map { GraduatedMemberDto.from(it) }
    }

    fun readAllWithdrawn(): List<WithdrawnMemberDto> {
        val members: List<WithdrawnMember> = memberReader.readAllWithdrawn()

        return members.map { WithdrawnMemberDto.from(it) }
    }

    fun searchAllActiveByNameOrNickname(query: String): List<ActiveMemberDto> {
        val members: List<ActiveMember> = memberReader.searchAllActiveByNameOrNickname(query)

        return members.map { ActiveMemberDto.from(it) }
    }

    fun searchAllInactiveByNameOrNickname(query: String): List<InactiveMemberDto> {
        val members: List<InactiveMember> = memberReader.searchAllInactiveByNameOrNickname(query)

        return members.map { InactiveMemberDto.from(it) }
    }

    fun searchAllGraduatedByNameOrNickname(query: String): List<GraduatedMemberDto> {
        val members: List<GraduatedMember> = memberReader.searchAllGraduatedByNameOrNickname(query)

        return members.map { GraduatedMemberDto.from(it) }
    }

    fun searchAllWithdrawnByNameOrNickname(query: String): List<WithdrawnMemberDto> {
        val members: List<WithdrawnMember> = memberReader.searchAllWithdrawnByNameOrNickname(query)

        return members.map { WithdrawnMemberDto.from(it) }
    }

    fun readAllRoles(): List<String> {
        val customOrder = listOf(
            MemberRole.LEAD,
            MemberRole.VICE_LEAD,
            MemberRole.MEMBER
        )

        return customOrder.map { MemberRoleConverter.convertToString(it) }
    }

    fun readAllStates(): List<String> {
        val customOrder = listOf(
            MemberState.ACTIVE,
            MemberState.INACTIVE,
            MemberState.GRADUATED,
            MemberState.WITHDRAWN
        )

        return customOrder.map { MemberStateConverter.convertToString(it) }
    }
}

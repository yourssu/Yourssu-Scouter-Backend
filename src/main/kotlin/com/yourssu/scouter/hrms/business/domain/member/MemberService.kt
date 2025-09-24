package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import com.yourssu.scouter.hrms.business.support.exception.IllegalMemberUpdateException
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberWriter: MemberWriter,
    private val memberReader: MemberReader,
    private val departmentReader: DepartmentReader,
    private val partReader: PartReader,
    private val semesterReader: SemesterReader,
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

    fun createMemberWithActiveStateIfNotExists(newMember: Member): Boolean {
        if (memberReader.existsByStudentId(newMember.studentId)) {
            return false
        }

        memberWriter.writeMemberWithActiveStatus(newMember, false)
        return true
    }

    fun readAllActiveByFilters(
        search: String?,
        partId: Long?,
    ): List<ActiveMemberDto> {
        var members: List<ActiveMember> = if (search.isNullOrEmpty()) {
            memberReader.readAllActive()
        } else {
            memberReader.searchAllActiveByNameOrNickname(search)
        }

        if (partId != null) {
            members = members.filter { it.member.parts.any { part -> part.id == partId } }
        }

        return members.sorted().map { ActiveMemberDto.from(it) }
    }

    fun readAllInActiveByFilters(
        search: String?,
        partId: Long?,
    ): List<InactiveMemberDto> {
        var members: List<InactiveMember> = if (search.isNullOrEmpty()) {
            memberReader.readAllInactive()
        } else {
            memberReader.searchAllInactiveByNameOrNickname(search)
        }

        if (partId != null) {
            members = members.filter { it.member.parts.any { part -> part.id == partId } }
        }

        return members.sorted().map { InactiveMemberDto.from(it) }
    }

    fun readAllGraduatedByFilters(
        search: String?,
        partId: Long?,
    ): List<GraduatedMemberDto> {
        var members: List<GraduatedMember> = if (search.isNullOrEmpty()) {
            memberReader.readAllGraduated()
        } else {
            memberReader.searchAllGraduatedByNameOrNickname(search)
        }

        if (partId != null) {
            members = members.filter { it.member.parts.any { part -> part.id == partId } }
        }

        return members.sorted().map { GraduatedMemberDto.from(it) }
    }

    fun readAllWithdrawnByFilters(
        search: String?,
        partId: Long?,
    ): List<WithdrawnMemberDto> {
        var members: List<WithdrawnMember> = if (search.isNullOrEmpty()) {
            memberReader.readAllWithdrawn()
        } else {
            memberReader.searchAllWithdrawnByNameOrNickname(search)
        }

        if (partId != null) {
            members = members.filter { it.member.parts.any { part -> part.id == partId } }
        }

        return members.sorted().map { WithdrawnMemberDto.from(it) }
    }

    fun updateActiveById(command: UpdateActiveMemberCommand) {
        validateUpdateFieldCountIsOne(
            command.updateMemberInfoCommand,
            command.isMembershipFeePaid,
        )

        if (command.updateMemberInfoCommand != null) {
            updateMemberInfo(command.updateMemberInfoCommand)

            return
        }

        val target: ActiveMember = memberReader.readActiveByMemberId(command.targetMemberId)
        val updated = ActiveMember(
            id = target.id,
            member = target.member,
            isMembershipFeePaid = command.isMembershipFeePaid ?: target.isMembershipFeePaid,
        )

        memberWriter.update(updated)
    }

    private fun <T> validateUpdateFieldCountIsOne(vararg updateFields: T?) {
        val updateFieldCount: Int = updateFields.count { it != null }
        if (updateFieldCount > 1) {
            throw IllegalMemberUpdateException("한 번에 하나의 필드만 수정할 수 있습니다.")
        }
    }

    fun updateInactiveById(command: UpdateInactiveMemberCommand) {
        validateUpdateFieldCountIsOne(
            command.updateMemberInfoCommand,
            command.expectedReturnSemesterId,
        )

        if (command.updateMemberInfoCommand != null) {
            updateMemberInfo(command.updateMemberInfoCommand)

            return
        }

        val target: InactiveMember = memberReader.readInactiveByMemberId(command.targetMemberId)
        if (command.expectedReturnSemesterId != null) {
            val expectedReturnSemester: Semester = semesterReader.readById(command.expectedReturnSemesterId)
            val previousSemesterBeforeExpectedReturnSemester: Semester =
                semesterReader.read(expectedReturnSemester.previous())

            val updated = target.updateExpectedReturnSemester(
                expectedReturnSemester = expectedReturnSemester,
                previousSemesterBeforeExpectedReturnSemester = previousSemesterBeforeExpectedReturnSemester,
            )

            memberWriter.update(updated)

            return
        }
    }

    fun updateGraduatedById(command: UpdateGraduatedMemberCommand) {
        validateUpdateFieldCountIsOne(
            command.updateMemberInfoCommand,
            command.isAdvisorDesired,
        )

        if (command.updateMemberInfoCommand != null) {
            updateMemberInfo(command.updateMemberInfoCommand)

            return
        }

        val target: GraduatedMember = memberReader.readGraduatedByMemberId(command.targetMemberId)
        val updated = GraduatedMember(
            id = target.id,
            member = target.member,
            activePeriod = target.activePeriod,
            isAdvisorDesired = command.isAdvisorDesired ?: target.isAdvisorDesired,
        )

        memberWriter.update(updated)
    }

    fun updateWithdrawnById(command: UpdateWithdrawnMemberCommand) {
        validateUpdateFieldCountIsOne(
            command.updateMemberInfoCommand,
        )

        if (command.updateMemberInfoCommand != null) {
            updateMemberInfo(command.updateMemberInfoCommand)

            return
        }
    }

    private fun updateMemberInfo(command: UpdateMemberInfoCommand) {
        val target: Member = memberReader.readById(command.targetMemberId)
        if (command.role != null) {
            updateRole(target, command.role)
            return
        }

        if (command.state != null) {
            deletePreviousStateData(target)
            updateState(target, command.state)
            return
        }

        if (!command.partIds.isNullOrEmpty()) {
            val newParts: SortedSet<Part> = partReader.readAllByIds(command.partIds).toSortedSet()
            updateParts(target, newParts)
            return
        }

        val updateMember = Member(
            id = target.id,
            name = command.name ?: target.name,
            email = command.email ?: target.email,
            phoneNumber = command.phoneNumber ?: target.phoneNumber,
            birthDate = command.birthDate ?: target.birthDate,
            department = command.departmentId?.let { departmentReader.readById(it) } ?: target.department,
            studentId = command.studentId ?: target.studentId,
            parts = target.parts,
            role = target.role,
            nicknameEnglish = command.nicknameEnglish ?: target.nicknameEnglish,
            nicknameKorean = command.nicknameKorean ?: target.nicknameKorean,
            state = target.state,
            joinDate = command.joinDate ?: target.joinDate,
            note = command.note ?: target.note,
            stateUpdatedTime = target.stateUpdatedTime,
        )

        memberWriter.update(updateMember)
    }

    private fun updateRole(target: Member, newRole: MemberRole) {
        target.updateRole(newRole)
        memberWriter.update(target)
    }

    private fun deletePreviousStateData(target: Member) {
        when (target.state) {
            MemberState.ACTIVE -> memberWriter.deleteFromActiveMember(target)
            MemberState.INACTIVE -> memberWriter.deleteFromInactiveMember(target)
            MemberState.GRADUATED -> memberWriter.deleteFromGraduatedMember(target)
            MemberState.WITHDRAWN -> memberWriter.deleteFromWithdrawnMember(target)
        }
    }

    private fun updateState(target: Member, newState: MemberState) {
        target.updateState(newState, LocalDateTime.now())

        when (newState) {
            MemberState.ACTIVE -> {
                memberWriter.writeMemberWithActiveStatus(
                    member = target,
                    isMembershipFeePaid = false,
                )
            }

            MemberState.INACTIVE -> {
                memberWriter.writeMemberWithInactiveState(
                    member = target,
                    currentDate = LocalDate.now(),
                )
            }

            MemberState.GRADUATED -> {
                memberWriter.writeMemberWithGraduatedState(
                    member = target,
                    currentDate = LocalDate.now(),
                )
            }

            MemberState.WITHDRAWN -> {
                memberWriter.writeMemberWithWithdrawnState(target)
            }
        }
    }

    private fun updateParts(target: Member, newParts: SortedSet<Part>) {
        target.updateParts(newParts)
        memberWriter.update(target)
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

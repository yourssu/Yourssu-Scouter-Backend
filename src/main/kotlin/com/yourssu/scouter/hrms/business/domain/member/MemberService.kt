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
        )

        return writtenActiveMember.id!!
    }

    fun createMemberWithActiveStateIfNotExists(newMember: Member) {
        if (memberReader.existsByStudentId(newMember.studentId)) {
            return
        }

        memberWriter.writeMemberWithActiveStatus(newMember)
    }

    fun readAllActive(): List<ActiveMemberDto> {
        val members: List<ActiveMember> = memberReader.readAllActive().sorted()

        return members.map { ActiveMemberDto.from(it) }
    }

    fun readAllInactive(): List<InactiveMemberDto> {
        val members: List<InactiveMember> = memberReader.readAllInactive().sorted()

        return members.map { InactiveMemberDto.from(it) }
    }

    fun readAllGraduated(): List<GraduatedMemberDto> {
        val members: List<GraduatedMember> = memberReader.readAllGraduated().sorted()

        return members.map { GraduatedMemberDto.from(it) }
    }

    fun readAllWithdrawn(): List<WithdrawnMemberDto> {
        val members: List<WithdrawnMember> = memberReader.readAllWithdrawn().sorted()

        return members.map { WithdrawnMemberDto.from(it) }
    }

    fun searchAllActiveByNameOrNickname(query: String): List<ActiveMemberDto> {
        val members: List<ActiveMember> = memberReader.searchAllActiveByNameOrNickname(query).sorted()

        return members.map { ActiveMemberDto.from(it) }
    }

    fun searchAllInactiveByNameOrNickname(query: String): List<InactiveMemberDto> {
        val members: List<InactiveMember> = memberReader.searchAllInactiveByNameOrNickname(query).sorted()

        return members.map { InactiveMemberDto.from(it) }
    }

    fun searchAllGraduatedByNameOrNickname(query: String): List<GraduatedMemberDto> {
        val members: List<GraduatedMember> = memberReader.searchAllGraduatedByNameOrNickname(query).sorted()

        return members.map { GraduatedMemberDto.from(it) }
    }

    fun searchAllWithdrawnByNameOrNickname(query: String): List<WithdrawnMemberDto> {
        val members: List<WithdrawnMember> = memberReader.searchAllWithdrawnByNameOrNickname(query).sorted()

        return members.map { WithdrawnMemberDto.from(it) }
    }

    fun updateActiveById(command: UpdateActiveMemberCommand) {
        if (countFilledFields(command) > 1) {
            throw IllegalMemberUpdateException("한 번에 하나의 필드만 수정할 수 있습니다.")
        }

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

    private fun countFilledFields(command: UpdateActiveMemberCommand): Int {
        return listOf(
            command.updateMemberInfoCommand,
            command.isMembershipFeePaid,
        ).count { it != null }
    }

    fun updateInactiveById(command: UpdateInactiveMemberCommand) {
        if (countFilledFields(command) > 1) {
            throw IllegalMemberUpdateException("한 번에 하나의 필드만 수정할 수 있습니다.")
        }

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

    private fun countFilledFields(command: UpdateInactiveMemberCommand): Int {
        return listOf(
            command.updateMemberInfoCommand,
            command.expectedReturnSemesterId,
        ).count { it != null }
    }

    fun updateGraduatedById(command: UpdateGraduatedMemberCommand) {
        if (countFilledFields(command) > 1) {
            throw IllegalMemberUpdateException("한 번에 하나의 필드만 수정할 수 있습니다.")
        }

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

    private fun countFilledFields(command: UpdateGraduatedMemberCommand): Int {
        return listOf(
            command.updateMemberInfoCommand,
            command.isAdvisorDesired,
        ).count { it != null }
    }

    fun updateWithdrawnById(command: UpdateWithdrawnMemberCommand) {
        if (countFilledFields(command) > 1) {
            throw IllegalMemberUpdateException("한 번에 하나의 필드만 수정할 수 있습니다.")
        }

        if (command.updateMemberInfoCommand != null) {
            updateMemberInfo(command.updateMemberInfoCommand)

            return
        }
    }

    private fun countFilledFields(command: UpdateWithdrawnMemberCommand): Int {
        return listOf(
            command.updateMemberInfoCommand,
        ).count { it != null }
    }

    private fun updateMemberInfo(command: UpdateMemberInfoCommand) {
        val target: Member = memberReader.readById(command.targetMemberId)
        if (command.role != null) {
            val newRole: MemberRole = command.role

            target.updateRole(newRole)
            memberWriter.update(target)

            return
        }

        if (command.state != null) {
            val newState: MemberState = command.state

            deletePreviousStateData(target)
            target.updateState(newState, LocalDateTime.now())
            updateNewStateData(newState, target)

            return
        }

        if (!command.partIds.isNullOrEmpty()) {
            val newParts: SortedSet<Part> = partReader.readAllByIds(command.partIds).toSortedSet()
            target.updateParts(newParts)
            memberWriter.update(target)

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

    private fun deletePreviousStateData(target: Member) {
        when (target.state) {
            MemberState.ACTIVE -> memberWriter.deleteFromActiveMember(target)
            MemberState.INACTIVE -> memberWriter.deleteFromInactiveMember(target)
            MemberState.GRADUATED -> memberWriter.deleteFromGraduatedMember(target)
            MemberState.WITHDRAWN -> memberWriter.deleteFromWithdrawnMember(target)
        }
    }

    private fun updateNewStateData(
        newState: MemberState,
        updateMember: Member
    ) {
        when (newState) {
            MemberState.ACTIVE -> {
                memberWriter.writeMemberWithActiveStatus(
                    member = updateMember,
                )
            }

            MemberState.INACTIVE -> {
                memberWriter.writeMemberWithInactiveState(
                    member = updateMember,
                    currentDate = LocalDate.now(),
                )
            }

            MemberState.GRADUATED -> {
                memberWriter.writeMemberWithGraduatedState(
                    member = updateMember,
                    currentDate = LocalDate.now(),
                )
            }

            MemberState.WITHDRAWN -> {
                memberWriter.writeMemberWithWithdrawnState(updateMember)
            }
        }
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

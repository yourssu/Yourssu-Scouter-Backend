package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.business.support.utils.SemesterConverter
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import com.yourssu.scouter.common.implement.support.exception.SemesterNotFoundException
import com.yourssu.scouter.hrms.business.support.exception.IllegalMemberUpdateException
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.implement.domain.member.*
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.*

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

    fun readAllCompletedByFilters(
        search: String?,
        partId: Long?,
    ): List<CompletedMemberDto> {
        var members: List<CompletedMember> = if (search.isNullOrEmpty()) {
            memberReader.readAllCompleted()
        } else {
            memberReader.searchAllCompletedByNameOrNickname(search)
        }

        if (partId != null) {
            members = members.filter { it.member.parts.any { part -> part.id == partId } }
        }

        return members.sorted().map { CompletedMemberDto.from(it) }
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
        validateUpdateActiveCommand(command)

        if (command.updateMemberInfoCommand != null) {
            updateMemberInfo(command.updateMemberInfoCommand)

            return
        }

        val target: ActiveMember = memberReader.readActiveByMemberId(command.targetMemberId)
        val updated = ActiveMember(
            id = target.id,
            member = target.member,
            isMembershipFeePaid = command.isMembershipFeePaid ?: target.isMembershipFeePaid,
            grade = command.grade ?: target.grade,
            isOnLeave = command.isOnLeave ?: target.isOnLeave,
        )

        memberWriter.update(updated)
    }

    private fun validateUpdateActiveCommand(command: UpdateActiveMemberCommand) {
        val hasMemberInfo = command.updateMemberInfoCommand != null
        val hasActiveOnlyField = command.isMembershipFeePaid != null ||
            command.grade != null ||
            command.isOnLeave != null
        if (hasMemberInfo && hasActiveOnlyField) {
            throw IllegalMemberUpdateException("회원 정보 수정과 액티브 전용 필드(회비/학년/재휴학) 수정을 동시에 요청할 수 없습니다.")
        }
        if (!hasMemberInfo && !hasActiveOnlyField) {
            throw IllegalMemberUpdateException("수정할 필드를 하나 이상 지정해야 합니다.")
        }
    }

    private fun <T> validateUpdateFieldCountIsOne(vararg updateFields: T?) {
        val updateFieldCount: Int = updateFields.count { it != null }
        if (updateFieldCount > 1) {
            throw IllegalMemberUpdateException("한 번에 하나의 필드만 수정할 수 있습니다.")
        }
    }

    fun updateInactiveById(command: UpdateInactiveMemberCommand) {
        val metadataSpecified = command.inactiveMetadataPatch?.isSpecified() == true
        validateUpdateFieldCountIsOne(
            command.updateMemberInfoCommand,
            command.inactiveMetadataPatch.takeIf { metadataSpecified },
        )

        if (command.updateMemberInfoCommand != null) {
            updateMemberInfo(command.updateMemberInfoCommand)

            return
        }

        if (metadataSpecified) {
            applyInactiveMetadataPatch(command.targetMemberId, command.inactiveMetadataPatch!!)

            return
        }

        throw IllegalMemberUpdateException("수정할 필드를 하나 이상 지정해야 합니다.")
    }

    private fun applyInactiveMetadataPatch(memberId: Long, patch: UpdateInactiveMemberMetadataPatch) {
        var base: InactiveMember = memberReader.readInactiveByMemberId(memberId)

        if (patch.expectedReturnSemester != null) {
            val newExpected: Semester = semesterReader.readByString(patch.expectedReturnSemester.trim())
            if (newExpected != base.expectedReturnSemester) {
                val previousBeforeReturn: Semester = try {
                    semesterReader.read(newExpected.previous())
                } catch (_: SemesterNotFoundException) {
                    base.inactivePeriod.endSemester
                }
                base = base.updateExpectedReturnSemester(newExpected, previousBeforeReturn)
            }
        }

        val updated = InactiveMember(
            id = base.id,
            member = base.member,
            activePeriod = base.activePeriod,
            expectedReturnSemester = base.expectedReturnSemester,
            inactivePeriod = base.inactivePeriod,
            reason = mergePatchedNullableString(patch.reason, base.reason),
            smsReplied = patch.smsReplied ?: base.smsReplied,
            smsReplyDesiredPeriod = mergePatchedNullableString(patch.smsReplyDesiredPeriod, base.smsReplyDesiredPeriod),
            activitySemestersLabel = mergePatchedNullableString(patch.activitySemestersLabel, base.activitySemestersLabel),
            totalActiveSemesters = patch.totalActiveSemesters ?: base.totalActiveSemesters,
            totalInactiveSemesters = patch.totalInactiveSemesters ?: base.totalInactiveSemesters,
        )

        memberWriter.update(updated)
    }

    /** patch가 null이면 유지, 오면 trim 후 빈 문자열이면 null 저장 */
    private fun mergePatchedNullableString(patch: String?, current: String?): String? {
        if (patch == null) {
            return current
        }
        val trimmed = patch.trim()
        return trimmed.takeIf { it.isNotEmpty() }
    }

    fun updateCompletedById(command: UpdateCompletedMemberCommand) {
        val completionPatch: String? = command.completionSemester?.takeIf { it.isNotBlank() }
        validateUpdateFieldCountIsOne(
            command.updateMemberInfoCommand,
            completionPatch,
        )

        if (command.updateMemberInfoCommand != null) {
            updateMemberInfo(command.updateMemberInfoCommand)

            return
        }

        if (completionPatch != null) {
            applyCompletedCompletionSemesterPatch(command.targetMemberId, completionPatch.trim())

            return
        }

        throw IllegalMemberUpdateException("수정할 필드를 하나 이상 지정해야 합니다.")
    }

    private fun applyCompletedCompletionSemesterPatch(memberId: Long, semesterStr: String) {
        val target: CompletedMember = memberReader.readCompletedByMemberId(memberId)
        val semester: Semester = semesterReader.readByString(semesterStr)
        memberWriter.update(
            CompletedMember(
                id = target.id,
                member = target.member,
                completionSemester = semester,
            ),
        )
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

        if (command.isAdvisorDesired != null) {
            val target: GraduatedMember = memberReader.readGraduatedByMemberId(command.targetMemberId)
            val updated = GraduatedMember(
                id = target.id,
                member = target.member,
                activePeriod = target.activePeriod,
                isAdvisorDesired = command.isAdvisorDesired,
            )

            memberWriter.update(updated)

            return
        }

        throw IllegalMemberUpdateException("수정할 필드를 하나 이상 지정해야 합니다.")
    }

    fun updateWithdrawnById(command: UpdateWithdrawnMemberCommand) {
        validateUpdateFieldCountIsOne(
            command.updateMemberInfoCommand,
            command.withdrawnDate,
        )

        if (command.updateMemberInfoCommand != null) {
            updateMemberInfo(command.updateMemberInfoCommand)

            return
        }

        if (command.withdrawnDate != null) {
            val target: WithdrawnMember = memberReader.readWithdrawnByMemberId(command.targetMemberId)
            val updated = WithdrawnMember(
                id = target.id,
                member = target.member,
                withdrawnDate = command.withdrawnDate,
            )
            memberWriter.update(updated)

            return
        }

        throw IllegalMemberUpdateException("수정할 필드를 하나 이상 지정해야 합니다.")
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
            MemberState.COMPLETED -> memberWriter.deleteFromCompletedMember(target)
            MemberState.GRADUATED -> memberWriter.deleteFromGraduatedMember(target)
            MemberState.WITHDRAWN -> memberWriter.deleteFromWithdrawnMember(target)
        }
    }

    private fun updateState(target: Member, newState: MemberState) {
        target.updateState(newState, Instant.now())

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

            MemberState.COMPLETED -> {
                val completionSemester = semesterReader.readByString(
                    SemesterConverter.convertToIntString(LocalDate.now()),
                )
                memberWriter.writeMemberWithCompletedState(
                    member = target,
                    completionSemester = completionSemester,
                )
            }

            MemberState.GRADUATED -> {
                memberWriter.writeMemberWithGraduatedState(
                    member = target,
                    currentDate = LocalDate.now(),
                )
            }

            MemberState.WITHDRAWN -> {
                memberWriter.writeMemberWithWithdrawnState(target, LocalDate.now())
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
            MemberState.COMPLETED,
            MemberState.GRADUATED,
            MemberState.WITHDRAWN
        )

        return customOrder.map { MemberStateConverter.convertToString(it) }
    }
}

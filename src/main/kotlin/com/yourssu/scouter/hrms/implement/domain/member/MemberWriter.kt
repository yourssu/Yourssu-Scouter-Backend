package com.yourssu.scouter.hrms.implement.domain.member

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import com.yourssu.scouter.common.implement.support.exception.SemesterNotFoundException
import java.time.LocalDate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MemberWriter(
    private val memberRepository: MemberRepository,
    private val semesterRepository: SemesterRepository,
    private val activeMemberRepository: ActiveMemberRepository,
    private val inactiveMemberRepository: InactiveMemberRepository,
    private val completedMemberRepository: CompletedMemberRepository,
    private val graduatedMemberRepository: GraduatedMemberRepository,
    private val withdrawnMemberRepository: WithdrawnMemberRepository,
) {

    fun writeMemberWithActiveStatus(
        member: Member,
        isMembershipFeePaid: Boolean,
        grade: Int? = null,
        isOnLeave: Boolean? = null,
    ): ActiveMember {
        val savedMember: Member = memberRepository.save(member)
        val activeMember = ActiveMember(
            member = savedMember,
            isMembershipFeePaid = isMembershipFeePaid,
            grade = grade,
            isOnLeave = isOnLeave,
        )

        return activeMemberRepository.save(activeMember)
    }

    fun writeMemberWithInactiveState(
        member: Member,
        currentDate: LocalDate,
        reason: String? = null,
        inactiveSemesterStr: String? = null,
        expectedReturnSemesterStr: String? = null,
        smsReplied: Boolean? = null,
        smsReplyDesiredPeriod: String? = null,
    ) {
        val savedMember: Member = memberRepository.save(member)
        val joinSemester: Semester = semesterRepository.find(Semester.of(member.joinDate))
            ?: throw SemesterNotFoundException("가입 날짜 '${member.joinDate}'에 해당하는 학기가 존재하지 않습니다.")
        val stateChangeSemester: Semester = if (!inactiveSemesterStr.isNullOrBlank()) {
            semesterRepository.find(Semester.of(inactiveSemesterStr.trim()))
                ?: throw SemesterNotFoundException("활동 학기 '$inactiveSemesterStr'에 해당하는 학기가 존재하지 않습니다.")
        } else {
            semesterRepository.find(Semester.of(currentDate))
                ?: throw SemesterNotFoundException("상태 변경 날짜에 해당하는 학기가 존재하지 않습니다.")
        }
        val previousSemesterBeforeStateChange: Semester = semesterRepository.find(stateChangeSemester.previous())
            ?: throw SemesterNotFoundException("상태 변경 날짜 이전 학기가 존재하지 않습니다.")
        val nextSemesterAfterStateChange: Semester = if (!expectedReturnSemesterStr.isNullOrBlank()) {
            semesterRepository.find(Semester.of(expectedReturnSemesterStr.trim()))
                ?: throw SemesterNotFoundException("예정복귀 시기 '$expectedReturnSemesterStr'에 해당하는 학기가 존재하지 않습니다.")
        } else {
            semesterRepository.find(stateChangeSemester.next())
                ?: throw SemesterNotFoundException("상태 변경 날짜 이후 학기가 존재하지 않습니다.")
        }
        val inactiveMember = InactiveMember(
            id = null,
            member = savedMember,
            activePeriod = SemesterPeriod(
                startSemester = joinSemester,
                endSemester = previousSemesterBeforeStateChange,
            ),
            expectedReturnSemester = nextSemesterAfterStateChange,
            inactivePeriod = SemesterPeriod(
                startSemester = stateChangeSemester,
                endSemester = stateChangeSemester,
            ),
            reason = reason?.takeIf { it.isNotBlank() },
            smsReplied = smsReplied,
            smsReplyDesiredPeriod = smsReplyDesiredPeriod?.takeIf { it.isNotBlank() },
        )

        inactiveMemberRepository.save(inactiveMember)
    }

    fun writeMemberWithCompletedState(member: Member, completionSemester: Semester) {
        val savedMember: Member = memberRepository.save(member)
        completedMemberRepository.deleteByMemberId(savedMember.id!!)
        val completedMember = CompletedMember(
            id = null,
            member = savedMember,
            completionSemester = completionSemester,
            isAdvisorDesired = false,
        )
        completedMemberRepository.save(completedMember)
    }

    fun writeMemberWithGraduatedState(member: Member, currentDate: LocalDate) {
        val savedMember: Member = memberRepository.save(member)
        graduatedMemberRepository.deleteByMemberId(savedMember.id!!)
        val joinSemester: Semester = semesterRepository.find(Semester.of(member.joinDate))
            ?: throw SemesterNotFoundException("가입 날짜 '${member.joinDate}'에 해당하는 학기가 존재하지 않습니다.")
        val previousSemesterBeforeStateChange: Semester = semesterRepository.find(Semester.previous(currentDate))
            ?: throw SemesterNotFoundException("상태 변경 날짜 이전 학기가 존재하지 않습니다.")
        val graduatedMember = GraduatedMember(
            id = null,
            member = savedMember,
            activePeriod = SemesterPeriod(
                startSemester = joinSemester,
                endSemester = previousSemesterBeforeStateChange,
            ),
            isAdvisorDesired = false,
        )
        graduatedMemberRepository.save(graduatedMember)
    }

    fun writeMemberWithGraduatedState(member: Member, graduateSemester: Semester) {
        val savedMember: Member = memberRepository.save(member)
        graduatedMemberRepository.deleteByMemberId(savedMember.id!!)
        val joinSemester: Semester = semesterRepository.find(Semester.of(member.joinDate))
            ?: throw SemesterNotFoundException("가입 날짜 '${member.joinDate}'에 해당하는 학기가 존재하지 않습니다.")
        val previousSemesterBeforeStateChange: Semester = semesterRepository.find(graduateSemester.previous())
            ?: throw SemesterNotFoundException("졸업 학기 이전 학기가 존재하지 않습니다.")
        val graduatedMember = GraduatedMember(
            id = null,
            member = savedMember,
            activePeriod = SemesterPeriod(
                startSemester = joinSemester,
                endSemester = previousSemesterBeforeStateChange,
            ),
            isAdvisorDesired = false,
        )
        graduatedMemberRepository.save(graduatedMember)
    }

    fun writeMemberWithWithdrawnState(updateMember: Member, withdrawnDate: LocalDate? = null) {
        val savedMember: Member = memberRepository.save(updateMember)
        withdrawnMemberRepository.deleteByMemberId(savedMember.id!!)
        val withdrawnMember = WithdrawnMember(
            member = savedMember,
            withdrawnDate = withdrawnDate,
        )

        withdrawnMemberRepository.save(withdrawnMember)
    }

    fun update(toUpdate: Member) {
        memberRepository.save(toUpdate)
    }

    fun update(toUpdate: ActiveMember) {
        memberRepository.save(toUpdate.member)
        activeMemberRepository.save(toUpdate)
    }

    fun update(toUpdate: InactiveMember) {
        memberRepository.save(toUpdate.member)
        inactiveMemberRepository.save(toUpdate)
    }

    fun update(toUpdate: CompletedMember) {
        memberRepository.save(toUpdate.member)
        completedMemberRepository.save(toUpdate)
    }

    fun update(toUpdate: GraduatedMember) {
        memberRepository.save(toUpdate.member)
        graduatedMemberRepository.save(toUpdate)
    }

    fun update(toUpdate: WithdrawnMember) {
        memberRepository.save(toUpdate.member)
        withdrawnMemberRepository.save(toUpdate)
    }

    fun deleteFromActiveMember(target: Member) {
        activeMemberRepository.deleteByMemberId(target.id!!)
    }

    fun deleteFromInactiveMember(target: Member) {
        inactiveMemberRepository.deleteByMemberId(target.id!!)
    }

    fun deleteFromCompletedMember(target: Member) {
        completedMemberRepository.deleteByMemberId(target.id!!)
    }

    fun deleteFromGraduatedMember(target: Member) {
        graduatedMemberRepository.deleteByMemberId(target.id!!)
    }

    fun deleteFromWithdrawnMember(target: Member) {
        withdrawnMemberRepository.deleteByMemberId(target.id!!)
    }
}

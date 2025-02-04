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
    private val graduatedMemberRepository: GraduatedMemberRepository,
    private val withdrawnMemberRepository: WithdrawnMemberRepository,
) {

    fun writeMemberWithActiveStatus(member: Member): ActiveMember {
        val savedMember: Member = memberRepository.save(member)
        val activeMember = ActiveMember(
            member = savedMember,
        )

        return activeMemberRepository.save(activeMember)
    }

    fun writeMemberWithInactiveState(member: Member, currentDate: LocalDate) {
        val savedMember: Member = memberRepository.save(member)
        val joinSemester: Semester = semesterRepository.find(Semester.of(member.joinDate))
            ?: throw SemesterNotFoundException("가입 날짜에 해당하는 학기가 존재하지 않습니다.")
        val stateChangeSemester: Semester = semesterRepository.find(Semester.of(currentDate))
            ?: throw SemesterNotFoundException("상태 변경 날짜에 해당하는 학기가 존재하지 않습니다.")
        val previousSemesterBeforeStateChange: Semester = semesterRepository.find(stateChangeSemester.previous())
            ?: throw SemesterNotFoundException("상태 변경 날짜 이전 학기가 존재하지 않습니다.")
        val nextSemesterAfterStateChange: Semester = semesterRepository.find(stateChangeSemester.next())
            ?: throw SemesterNotFoundException("상태 변경 날짜 이후 학기가 존재하지 않습니다.")
        val inactiveMember = InactiveMember(
            member = savedMember,
            joinSemester = joinSemester,
            stateChangeSemester = stateChangeSemester,
            previousSemesterBeforeStateChange = previousSemesterBeforeStateChange,
            nextSemesterAfterStateChange = nextSemesterAfterStateChange,
        )

        inactiveMemberRepository.save(inactiveMember)
    }

    fun writeMemberWithGraduatedState(member: Member, currentDate: LocalDate) {
        val savedMember: Member = memberRepository.save(member)
        val joinSemester: Semester = semesterRepository.find(Semester.of(member.joinDate))
            ?: throw SemesterNotFoundException("가입 날짜에 해당하는 학기가 존재하지 않습니다.")
        val previousSemesterBeforeStateChange: Semester = semesterRepository.find(Semester.previous(currentDate))
            ?: throw SemesterNotFoundException("상태 변경 날짜 이전 학기가 존재하지 않습니다.")

        val graduatedMember = GraduatedMember(
            member = savedMember,
            joinSemester = joinSemester,
            previousSemesterBeforeStateChange = previousSemesterBeforeStateChange,
        )

        graduatedMemberRepository.save(graduatedMember)
    }

    fun writeMemberWithWithdrawnState(updateMember: Member) {
        val savedMember: Member = memberRepository.save(updateMember)
        val withdrawnMember = WithdrawnMember(
            member = savedMember,
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

    fun deleteFromGraduatedMember(target: Member) {
        graduatedMemberRepository.deleteByMemberId(target.id!!)
    }

    fun deleteFromWithdrawnMember(target: Member) {
        withdrawnMemberRepository.deleteByMemberId(target.id!!)
    }
}

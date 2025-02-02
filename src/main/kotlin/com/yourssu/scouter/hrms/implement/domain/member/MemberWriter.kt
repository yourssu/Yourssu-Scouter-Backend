package com.yourssu.scouter.hrms.implement.domain.member

import java.time.LocalDate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MemberWriter(
    private val memberRepository: MemberRepository,
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
        val inactiveMember = InactiveMember(
            member = savedMember,
            stateChangeDate = currentDate,
        )

        inactiveMemberRepository.save(inactiveMember)
    }

    fun writeMemberWithGraduatedState(member: Member, currentDate: LocalDate) {
        val savedMember: Member = memberRepository.save(member)
        val graduatedMember = GraduatedMember(
            member = savedMember,
            stateChangeDate = currentDate,
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
}

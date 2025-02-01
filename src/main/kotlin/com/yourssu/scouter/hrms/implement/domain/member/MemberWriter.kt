package com.yourssu.scouter.hrms.implement.domain.member

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MemberWriter(
    private val memberRepository: MemberRepository,
    private val activeMemberRepository: ActiveMemberRepository,
) {

    fun writeMemberWithActiveStatus(member: Member, isMembershipFeePaid: Boolean): ActiveMember {
        val savedMember: Member = memberRepository.save(member)
        val activeMember = ActiveMember(
            member = savedMember,
            isMembershipFeePaid = isMembershipFeePaid
        )

        return activeMemberRepository.save(activeMember)
    }
}

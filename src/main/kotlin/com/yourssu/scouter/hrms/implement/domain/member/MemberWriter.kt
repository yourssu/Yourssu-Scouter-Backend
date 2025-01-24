package com.yourssu.scouter.hrms.implement.domain.member

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MemberWriter(
    private val memberRepository: MemberRepository,
) {

    fun write(member: Member): Member {
        return memberRepository.save(member)
    }
}

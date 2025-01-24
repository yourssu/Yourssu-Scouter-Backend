package com.yourssu.scouter.hrms.implement.domain.member

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class MemberReader(
    private val memberRepository: MemberRepository,
) {

    fun readById(memberId: Long): Member =
        memberRepository.findById(memberId) ?: throw MemberNotFoundException("지정한 멤버를 찾을 수 없습니다.")

    fun readAll(): List<Member> {
        return memberRepository.findAll()
    }
}

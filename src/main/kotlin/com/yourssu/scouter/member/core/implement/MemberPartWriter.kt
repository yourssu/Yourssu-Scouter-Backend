package com.yourssu.scouter.member.core.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 파트 배정을 행 단위로 쓴다.
 * MemberRepository.save 는 member_part 를 통째로 지웠다 다시 넣는다. 파트 하나를 더하려고 그 경로를 타면
 * 호출자가 읽은 시점의 파트 집합으로 전체가 덮여, 그 사이 다른 운영자가 더한 파트가 사라진다.
 */
@Component
@Transactional
class MemberPartWriter(
    private val memberPartRepository: MemberPartRepository,
) {

    fun assign(memberId: Long, partId: Long) {
        memberPartRepository.assign(memberId, partId)
    }

    fun unassign(memberId: Long, partId: Long) {
        memberPartRepository.unassign(memberId, partId)
    }
}

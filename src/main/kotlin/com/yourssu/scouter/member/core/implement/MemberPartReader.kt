package com.yourssu.scouter.member.core.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class MemberPartReader(
    private val memberPartRepository: MemberPartRepository,
) {

    fun hasPart(memberId: Long, partId: Long): Boolean {
        return memberPartRepository.existsAssignment(memberId, partId)
    }

    fun countParts(memberId: Long): Long {
        return memberPartRepository.countDistinctParts(memberId)
    }
}

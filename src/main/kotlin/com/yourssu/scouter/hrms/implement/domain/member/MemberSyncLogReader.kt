package com.yourssu.scouter.hrms.implement.domain.member

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class MemberSyncLogReader(
    private val memberSyncLogRepository: MemberSyncLogRepository,
) {

    fun findLastLog(): MemberSyncLog? {
        return memberSyncLogRepository.findFirstByOrderBySyncTimeDesc()
    }
}

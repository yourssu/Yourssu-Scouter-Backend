package com.yourssu.scouter.hrms.implement.domain.member

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MemberSyncLogWriter(
    private val memberSyncLogRepository: MemberSyncLogRepository,
) {

    fun write(memberSyncLog: MemberSyncLog): MemberSyncLog {
        return memberSyncLogRepository.save(memberSyncLog)
    }
}

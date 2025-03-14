package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.MemberSyncLog
import com.yourssu.scouter.hrms.implement.domain.member.MemberSyncLogRepository
import org.springframework.stereotype.Repository

@Repository
class MemberSyncLogRepositoryImpl(
    private val jpaMemberSyncLogRepository: JpaMemberSyncLogRepository,
) : MemberSyncLogRepository {

    override fun save(memberSyncLog: MemberSyncLog): MemberSyncLog {
        return jpaMemberSyncLogRepository.save(MemberSyncLogEntity.from(memberSyncLog)).toDomain()
    }

    override fun findFirstByOrderBySyncTimeDesc(): MemberSyncLog? {
        return jpaMemberSyncLogRepository.findFirstByOrderBySyncTimeDesc()
    }
}

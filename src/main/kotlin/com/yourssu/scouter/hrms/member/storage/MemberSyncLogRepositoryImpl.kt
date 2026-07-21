package com.yourssu.scouter.hrms.member.storage

import com.yourssu.scouter.hrms.member.implement.MemberSyncLog
import com.yourssu.scouter.hrms.member.implement.MemberSyncLogRepository
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

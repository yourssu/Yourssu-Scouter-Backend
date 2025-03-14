package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.MemberSyncLog
import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberSyncLogRepository : JpaRepository<MemberSyncLogEntity, Long> {

    fun findFirstByOrderBySyncTimeDesc(): MemberSyncLog?
}

package com.yourssu.scouter.hrms.member.storage

import com.yourssu.scouter.hrms.member.implement.MemberSyncLog
import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberSyncLogRepository : JpaRepository<MemberSyncLogEntity, Long> {

    fun findFirstByOrderBySyncTimeDesc(): MemberSyncLog?
}

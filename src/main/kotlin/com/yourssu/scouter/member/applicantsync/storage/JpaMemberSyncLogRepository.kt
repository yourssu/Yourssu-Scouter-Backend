package com.yourssu.scouter.member.applicantsync.storage

import com.yourssu.scouter.member.applicantsync.implement.MemberSyncLog
import org.springframework.data.jpa.repository.JpaRepository

interface JpaMemberSyncLogRepository : JpaRepository<MemberSyncLogEntity, Long> {

    fun findFirstByOrderBySyncTimeDesc(): MemberSyncLog?
}

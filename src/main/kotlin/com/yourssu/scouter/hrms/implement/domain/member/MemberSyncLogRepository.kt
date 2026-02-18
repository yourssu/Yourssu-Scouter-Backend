package com.yourssu.scouter.hrms.implement.domain.member

interface MemberSyncLogRepository {

    fun save(memberSyncLog: MemberSyncLog): MemberSyncLog
    fun findFirstByOrderBySyncTimeDesc(): MemberSyncLog?
}

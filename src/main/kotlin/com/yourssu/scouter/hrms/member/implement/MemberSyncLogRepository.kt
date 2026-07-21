package com.yourssu.scouter.hrms.member.implement

interface MemberSyncLogRepository {

    fun save(memberSyncLog: MemberSyncLog): MemberSyncLog
    fun findFirstByOrderBySyncTimeDesc(): MemberSyncLog?
}

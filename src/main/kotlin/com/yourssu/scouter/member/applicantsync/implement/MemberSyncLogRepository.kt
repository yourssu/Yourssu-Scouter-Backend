package com.yourssu.scouter.member.applicantsync.implement

interface MemberSyncLogRepository {

    fun save(memberSyncLog: MemberSyncLog): MemberSyncLog
    fun findFirstByOrderBySyncTimeDesc(): MemberSyncLog?
}

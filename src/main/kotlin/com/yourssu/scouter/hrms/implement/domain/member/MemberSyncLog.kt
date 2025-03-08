package com.yourssu.scouter.hrms.implement.domain.member

import java.time.LocalDateTime

class MemberSyncLog(
    val id: Long? = null,
    val syncTime: LocalDateTime,
) {

    companion object {
        fun create(): MemberSyncLog {
            return MemberSyncLog(
                syncTime = LocalDateTime.now(),
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemberSyncLog

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

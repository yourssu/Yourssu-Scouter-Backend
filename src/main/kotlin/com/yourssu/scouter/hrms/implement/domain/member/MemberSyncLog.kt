package com.yourssu.scouter.hrms.implement.domain.member

import java.time.Instant

class MemberSyncLog(
    val id: Long? = null,
    val syncTime: Instant,
) {

    companion object {
        fun create(): MemberSyncLog {
            return MemberSyncLog(
                syncTime = Instant.now(),
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

package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.MemberSyncLog
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "member_sync_log")
class MemberSyncLogEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val syncTime: LocalDateTime,
) {

    companion object {
        fun from(memberSyncLog: MemberSyncLog): MemberSyncLogEntity {
            return MemberSyncLogEntity(
                id = memberSyncLog.id,
                syncTime = memberSyncLog.syncTime,
            )
        }
    }

    fun toDomain(): MemberSyncLog {
        return MemberSyncLog(
            id = id,
            syncTime = syncTime,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemberSyncLogEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

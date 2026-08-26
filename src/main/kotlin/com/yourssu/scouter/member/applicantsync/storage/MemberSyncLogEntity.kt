package com.yourssu.scouter.member.applicantsync.storage

import com.yourssu.scouter.member.applicantsync.implement.MemberSyncLog
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "member_sync_log")
class MemberSyncLogEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val syncTime: Instant,
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

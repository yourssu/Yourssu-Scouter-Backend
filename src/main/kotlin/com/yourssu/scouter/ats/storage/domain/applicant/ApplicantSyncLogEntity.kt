package com.yourssu.scouter.ats.storage.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncLog
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "applicant_sync_log")
class ApplicantSyncLogEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val applicationSemesterId: Long,

    @Column(nullable = false)
    val formId: String,

    @Column(nullable = false)
    val responseId: String,

    @Column(nullable = false)
    val syncTime: LocalDateTime,
) {

    companion object {
        fun from(applicantSyncLog: ApplicantSyncLog) = ApplicantSyncLogEntity(
            id = applicantSyncLog.id,
            applicationSemesterId = applicantSyncLog.applicationSemesterId,
            formId = applicantSyncLog.formId,
            responseId = applicantSyncLog.responseId,
            syncTime = applicantSyncLog.syncTime,
        )
    }

    fun toDomain(): ApplicantSyncLog {
        return ApplicantSyncLog(
            id = id,
            applicationSemesterId = applicationSemesterId,
            formId = formId,
            responseId = responseId,
            syncTime = syncTime,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApplicantSyncLogEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

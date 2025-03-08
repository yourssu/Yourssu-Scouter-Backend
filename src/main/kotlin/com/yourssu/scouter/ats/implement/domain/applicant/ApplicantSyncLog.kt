package com.yourssu.scouter.ats.implement.domain.applicant

import java.time.LocalDateTime

class ApplicantSyncLog(
    val id: Long? = null,
    val applicantSemesterId: Long,
    val formId: String,
    val responseId: String,
    val syncTime: LocalDateTime,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApplicantSyncLog

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

package com.yourssu.scouter.ats.implement.domain.applicant

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class ApplicantSyncLogWriter(
    private val applicantSyncLogRepository: ApplicantSyncLogRepository,
) {

    fun save(applicantSyncLog: ApplicantSyncLog): ApplicantSyncLog {
        return applicantSyncLogRepository.save(applicantSyncLog)
    }
}

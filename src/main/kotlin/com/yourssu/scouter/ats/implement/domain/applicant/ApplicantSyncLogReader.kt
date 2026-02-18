package com.yourssu.scouter.ats.implement.domain.applicant

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ApplicantSyncLogReader(
    private val applicantSyncLogRepository: ApplicantSyncLogRepository,
) {

    fun readAllByApplicationSemesterId(applicationSemesterId: Long): List<ApplicantSyncLog> {
        return applicantSyncLogRepository.findAllByApplicationSemesterId(applicationSemesterId)
    }

    fun findLastLog(): ApplicantSyncLog? {
        return applicantSyncLogRepository.findFirstByOrderBySyncTimeDesc()
    }
}

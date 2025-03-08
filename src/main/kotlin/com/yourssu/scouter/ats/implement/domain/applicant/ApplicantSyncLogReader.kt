package com.yourssu.scouter.ats.implement.domain.applicant

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ApplicantSyncLogReader(
    private val applicantSyncLogRepository: ApplicantSyncLogRepository,
) {

    fun findByApplicantSemesterId(applicantSemesterId: Long): List<ApplicantSyncLog> {
        return applicantSyncLogRepository.findAllByApplicantSemesterId(applicantSemesterId)
    }
}

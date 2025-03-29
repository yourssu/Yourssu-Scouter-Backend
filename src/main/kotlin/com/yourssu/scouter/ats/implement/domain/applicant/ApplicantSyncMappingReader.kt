package com.yourssu.scouter.ats.implement.domain.applicant

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ApplicantSyncMappingReader(
    private val applicantSyncMappingRepository: ApplicantSyncMappingRepository,
) {

    fun readAllByApplicantSemesterId(applicantSemesterId: Long): List<ApplicantSyncMapping> {
        return applicantSyncMappingRepository.findAllByApplicantSemesterId(applicantSemesterId)
    }
}

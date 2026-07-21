package com.yourssu.scouter.recruiting.applicant.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ApplicantAnswerReader(
    private val applicantAnswerRepository: ApplicantAnswerRepository,
) {
    fun readAllByApplicantId(applicantId: Long): List<ApplicantAnswer> {
        return applicantAnswerRepository.findAllByApplicantId(applicantId)
    }
}

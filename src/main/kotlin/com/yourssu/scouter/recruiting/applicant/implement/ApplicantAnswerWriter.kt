package com.yourssu.scouter.recruiting.applicant.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class ApplicantAnswerWriter(
    private val applicantAnswerRepository: ApplicantAnswerRepository,
) {
    fun writeAll(answers: List<ApplicantAnswer>) {
        applicantAnswerRepository.saveAll(answers)
    }
}

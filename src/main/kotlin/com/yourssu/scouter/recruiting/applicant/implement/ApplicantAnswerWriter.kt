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

    fun write(answer: ApplicantAnswer): ApplicantAnswer {
        return applicantAnswerRepository.save(answer)
    }

    fun delete(answerId: Long) {
        applicantAnswerRepository.deleteById(answerId)
    }

    fun deleteAllByApplicantId(applicantId: Long) {
        applicantAnswerRepository.deleteAllByApplicantId(applicantId)
    }
}

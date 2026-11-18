package com.yourssu.scouter.recruiting.applicant.implement

import com.yourssu.scouter.recruiting.support.implement.exception.ApplicantAnswerNotFoundException
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

    fun readById(answerId: Long): ApplicantAnswer {
        return applicantAnswerRepository.findById(answerId)
            ?: throw ApplicantAnswerNotFoundException("지정한 지원서 답변을 찾을 수 없습니다.")
    }
}

package com.yourssu.scouter.recruiting.evaluation.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class InterviewEvaluationWriter(
    private val interviewEvaluationRepository: InterviewEvaluationRepository,
) {
    fun write(evaluation: InterviewEvaluation): InterviewEvaluation {
        return interviewEvaluationRepository.save(evaluation)
    }

    fun deleteAllByApplicantId(applicantId: Long) {
        interviewEvaluationRepository.deleteAllByApplicantId(applicantId)
    }
}

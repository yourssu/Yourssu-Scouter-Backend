package com.yourssu.scouter.recruiting.evaluation.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class FinalEvaluationReader(
    private val finalEvaluationRepository: FinalEvaluationRepository,
) {
    fun readByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): FinalEvaluation? {
        return finalEvaluationRepository.findByApplicantIdAndMemberId(applicantId, evaluatorUserId)
    }

    fun readAllByApplicantId(applicantId: Long): List<FinalEvaluation> {
        return finalEvaluationRepository.findAllByApplicantId(applicantId)
    }

    fun readAllByApplicantIdIn(applicantIds: List<Long>): List<FinalEvaluation> {
        return finalEvaluationRepository.findAllByApplicantIdIn(applicantIds)
    }
}

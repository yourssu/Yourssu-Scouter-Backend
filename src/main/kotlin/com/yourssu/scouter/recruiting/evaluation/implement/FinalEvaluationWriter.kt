package com.yourssu.scouter.recruiting.evaluation.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class FinalEvaluationWriter(
    private val finalEvaluationRepository: FinalEvaluationRepository,
) {
    fun write(finalEvaluation: FinalEvaluation): FinalEvaluation {
        return finalEvaluationRepository.save(finalEvaluation)
    }

    fun deleteAllByApplicantId(applicantId: Long) {
        finalEvaluationRepository.deleteAllByApplicantId(applicantId)
    }
}

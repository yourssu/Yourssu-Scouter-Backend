package com.yourssu.scouter.recruiting.evaluation.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class DocumentEvaluationWriter(
    private val documentEvaluationRepository: DocumentEvaluationRepository,
) {

    fun write(evaluation: DocumentEvaluation): DocumentEvaluation {
        return documentEvaluationRepository.save(evaluation)
    }

    fun deleteAllByApplicantId(applicantId: Long) {
        documentEvaluationRepository.deleteAllByApplicantId(applicantId)
    }
}

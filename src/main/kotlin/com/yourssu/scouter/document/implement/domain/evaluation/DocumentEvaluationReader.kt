package com.yourssu.scouter.document.implement.domain.evaluation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class DocumentEvaluationReader(
    private val documentEvaluationRepository: DocumentEvaluationRepository,
) {

    fun readByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): DocumentEvaluation? {
        return documentEvaluationRepository.findByApplicantIdAndEvaluatorUserId(applicantId, evaluatorUserId)
    }

    fun readAllByApplicantId(applicantId: Long): List<DocumentEvaluation> {
        return documentEvaluationRepository.findAllByApplicantId(applicantId)
    }

    fun existsBySectionIdIn(sectionIds: List<Long>): Boolean {
        return documentEvaluationRepository.existsBySectionIdIn(sectionIds)
    }
}

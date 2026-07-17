package com.yourssu.scouter.document.implement.domain.evaluation

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
}

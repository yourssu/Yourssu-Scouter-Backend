package com.yourssu.scouter.document.implement.domain.evaluation

interface DocumentEvaluationRepository {

    fun save(evaluation: DocumentEvaluation): DocumentEvaluation

    fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): DocumentEvaluation?

    fun findAllByApplicantId(applicantId: Long): List<DocumentEvaluation>

    fun existsBySectionIdIn(sectionIds: List<Long>): Boolean
}

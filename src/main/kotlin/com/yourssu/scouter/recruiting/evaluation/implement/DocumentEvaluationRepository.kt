package com.yourssu.scouter.recruiting.evaluation.implement

interface DocumentEvaluationRepository {

    fun save(evaluation: DocumentEvaluation): DocumentEvaluation

    fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): DocumentEvaluation?

    fun findAllByApplicantId(applicantId: Long): List<DocumentEvaluation>

    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<DocumentEvaluation>

    fun existsBySectionIdIn(sectionIds: List<Long>): Boolean
}

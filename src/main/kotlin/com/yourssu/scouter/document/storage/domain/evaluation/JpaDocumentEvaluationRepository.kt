package com.yourssu.scouter.document.storage.domain.evaluation

import org.springframework.data.jpa.repository.JpaRepository

interface JpaDocumentEvaluationRepository : JpaRepository<DocumentEvaluationEntity, Long> {

    fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): DocumentEvaluationEntity?

    fun findAllByApplicantId(applicantId: Long): List<DocumentEvaluationEntity>
}

package com.yourssu.scouter.recruiting.evaluation.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaDocumentEvaluationRepository : JpaRepository<DocumentEvaluationEntity, Long> {

    fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): DocumentEvaluationEntity?

    fun findAllByApplicantId(applicantId: Long): List<DocumentEvaluationEntity>

    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<DocumentEvaluationEntity>

    fun deleteAllByApplicantId(applicantId: Long)
}

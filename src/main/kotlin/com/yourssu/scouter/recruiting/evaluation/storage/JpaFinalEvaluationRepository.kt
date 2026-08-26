package com.yourssu.scouter.recruiting.evaluation.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaFinalEvaluationRepository : JpaRepository<FinalEvaluationEntity, Long> {
    fun findByApplicantIdAndUserId(applicantId: Long, userId: Long): FinalEvaluationEntity?
    fun findAllByApplicantId(applicantId: Long): List<FinalEvaluationEntity>
    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<FinalEvaluationEntity>
    fun deleteAllByApplicantId(applicantId: Long)
}

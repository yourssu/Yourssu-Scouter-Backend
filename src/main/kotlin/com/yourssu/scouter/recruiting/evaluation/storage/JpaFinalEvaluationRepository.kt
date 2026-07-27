package com.yourssu.scouter.recruiting.evaluation.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaFinalEvaluationRepository : JpaRepository<FinalEvaluationEntity, Long> {
    fun findByApplicantIdAndMemberId(applicantId: Long, memberId: Long): FinalEvaluationEntity?
    fun findAllByApplicantId(applicantId: Long): List<FinalEvaluationEntity>
    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<FinalEvaluationEntity>
}

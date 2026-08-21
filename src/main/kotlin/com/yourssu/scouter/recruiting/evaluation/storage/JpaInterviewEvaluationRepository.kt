package com.yourssu.scouter.recruiting.evaluation.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaInterviewEvaluationRepository : JpaRepository<InterviewEvaluationEntity, Long> {
    fun findAllByApplicantIdAndUserId(applicantId: Long, userId: Long): List<InterviewEvaluationEntity>
    fun findAllByApplicantId(applicantId: Long): List<InterviewEvaluationEntity>
    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<InterviewEvaluationEntity>
    fun existsByInterviewEvaluationItemIdIn(itemIds: List<Long>): Boolean
    fun deleteAllByApplicantId(applicantId: Long)
}

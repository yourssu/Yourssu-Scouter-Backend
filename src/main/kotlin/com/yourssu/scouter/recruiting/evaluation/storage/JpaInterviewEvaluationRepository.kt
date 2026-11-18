package com.yourssu.scouter.recruiting.evaluation.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface JpaInterviewEvaluationRepository : JpaRepository<InterviewEvaluationEntity, Long> {
    fun findAllByApplicantIdAndUserId(applicantId: Long, userId: Long): List<InterviewEvaluationEntity>
    fun findAllByApplicantId(applicantId: Long): List<InterviewEvaluationEntity>
    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<InterviewEvaluationEntity>
    fun existsByInterviewEvaluationItemIdIn(itemIds: List<Long>): Boolean
    fun deleteAllByApplicantId(applicantId: Long)
    fun existsByApplicantId(applicantId: Long): Boolean

    @Query("SELECT COUNT(e) > 0 FROM InterviewEvaluationEntity e WHERE e.interviewEvaluationItem.interviewRubric.id = :rubricId")
    fun existsByInterviewRubricId(@Param("rubricId") rubricId: Long): Boolean

    @Query("SELECT DISTINCT e.interviewEvaluationItem.interviewRubric.id FROM InterviewEvaluationEntity e WHERE e.applicantId = :applicantId")
    fun findRubricIdsByApplicantId(@Param("applicantId") applicantId: Long): List<Long>
}

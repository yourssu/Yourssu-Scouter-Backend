package com.yourssu.scouter.recruiting.evaluation.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaFinalEvaluationRepository : JpaRepository<FinalEvaluationEntity, Long> {
    fun findByApplicantIdAndUserId(applicantId: Long, userId: Long): FinalEvaluationEntity?
    fun findAllByApplicantId(applicantId: Long): List<FinalEvaluationEntity>
    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<FinalEvaluationEntity>

    /** 제출된 면접 평가의 평균 점수를 지원자별로 집계한다. */
    @Query("""
        SELECT new com.yourssu.scouter.recruiting.evaluation.storage.InterviewAverageScoreRow(f.applicant.id, AVG(f.score))
        FROM FinalEvaluationEntity f
        WHERE f.applicant.id IN :applicantIds AND f.submittedAt IS NOT NULL
        GROUP BY f.applicant.id
    """)
    fun averageSubmittedScoresByApplicantIds(applicantIds: Collection<Long>): List<InterviewAverageScoreRow>

    fun deleteAllByApplicantId(applicantId: Long)
}

data class InterviewAverageScoreRow(
    val applicantId: Long,
    val average: Double,
)

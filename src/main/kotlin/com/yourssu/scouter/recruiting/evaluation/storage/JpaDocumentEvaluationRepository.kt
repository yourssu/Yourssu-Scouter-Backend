package com.yourssu.scouter.recruiting.evaluation.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaDocumentEvaluationRepository : JpaRepository<DocumentEvaluationEntity, Long> {

    fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): DocumentEvaluationEntity?

    fun findAllByApplicantId(applicantId: Long): List<DocumentEvaluationEntity>

    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<DocumentEvaluationEntity>

    /** 제출된 평가의 항목 점수 합계와 평가 수를 지원자별로 집계한다 (TEXT 컬럼을 읽지 않기 위한 집계 쿼리). 항목이 없는 평가는 0점으로 센다. */
    @Query("""
        SELECT new com.yourssu.scouter.recruiting.evaluation.storage.DocumentScoreSumRow(
            e.applicantId, SUM(i.score), COUNT(DISTINCT e.id))
        FROM DocumentEvaluationEntity e
        LEFT JOIN DocumentEvaluationItemEntity i ON i.evaluation = e
        WHERE e.applicantId IN :applicantIds AND e.submittedAt IS NOT NULL
        GROUP BY e.applicantId
    """)
    fun sumSubmittedScoresByApplicantIds(applicantIds: Collection<Long>): List<DocumentScoreSumRow>

    fun deleteAllByApplicantId(applicantId: Long)
}

data class DocumentScoreSumRow(
    val applicantId: Long,
    val scoreSum: Long?,
    val evaluationCount: Long,
)

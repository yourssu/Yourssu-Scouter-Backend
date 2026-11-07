package com.yourssu.scouter.recruiting.evaluation.implement

interface DocumentEvaluationRepository {

    fun save(evaluation: DocumentEvaluation): DocumentEvaluation

    fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): DocumentEvaluation?

    fun findAllByApplicantId(applicantId: Long): List<DocumentEvaluation>

    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<DocumentEvaluation>

    /** 제출된 서류 평가의 총점 평균을 지원자별로 반환한다. 제출된 평가가 없는 지원자는 포함되지 않는다. */
    fun findAverageSubmittedTotalScoreByApplicantIds(applicantIds: Collection<Long>): Map<Long, Double>

    fun existsBySectionIdIn(sectionIds: List<Long>): Boolean

    fun deleteAllByApplicantId(applicantId: Long)
}

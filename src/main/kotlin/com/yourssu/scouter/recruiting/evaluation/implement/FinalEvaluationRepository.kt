package com.yourssu.scouter.recruiting.evaluation.implement

interface FinalEvaluationRepository {
    fun save(finalEvaluation: FinalEvaluation): FinalEvaluation
    fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): FinalEvaluation?
    fun findAllByApplicantId(applicantId: Long): List<FinalEvaluation>
    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<FinalEvaluation>
    /** 제출된 면접 평가 점수의 평균을 지원자별로 반환한다. 제출된 평가가 없는 지원자는 포함되지 않는다. */
    fun findAverageSubmittedScoreByApplicantIds(applicantIds: Collection<Long>): Map<Long, Double>
    fun deleteAllByApplicantId(applicantId: Long)
}

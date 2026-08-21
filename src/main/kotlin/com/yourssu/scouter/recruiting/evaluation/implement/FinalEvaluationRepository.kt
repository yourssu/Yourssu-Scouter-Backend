package com.yourssu.scouter.recruiting.evaluation.implement

interface FinalEvaluationRepository {
    fun save(finalEvaluation: FinalEvaluation): FinalEvaluation
    fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): FinalEvaluation?
    fun findAllByApplicantId(applicantId: Long): List<FinalEvaluation>
    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<FinalEvaluation>
    fun deleteAllByApplicantId(applicantId: Long)
}

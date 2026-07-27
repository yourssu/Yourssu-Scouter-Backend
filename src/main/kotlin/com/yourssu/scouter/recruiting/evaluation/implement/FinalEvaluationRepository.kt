package com.yourssu.scouter.recruiting.evaluation.implement

interface FinalEvaluationRepository {
    fun save(finalEvaluation: FinalEvaluation): FinalEvaluation
    fun findByApplicantIdAndMemberId(applicantId: Long, memberId: Long): FinalEvaluation?
    fun findAllByApplicantId(applicantId: Long): List<FinalEvaluation>
    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<FinalEvaluation>
}

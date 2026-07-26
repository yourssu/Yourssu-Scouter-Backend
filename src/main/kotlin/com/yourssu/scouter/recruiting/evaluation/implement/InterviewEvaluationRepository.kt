package com.yourssu.scouter.recruiting.evaluation.implement

interface InterviewEvaluationRepository {
    fun save(evaluation: InterviewEvaluation): InterviewEvaluation
    fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): InterviewEvaluation?
    fun findAllByApplicantId(applicantId: Long): List<InterviewEvaluation>
    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<InterviewEvaluation>
}

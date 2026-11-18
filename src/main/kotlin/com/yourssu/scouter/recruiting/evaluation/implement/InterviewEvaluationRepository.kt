package com.yourssu.scouter.recruiting.evaluation.implement

interface InterviewEvaluationRepository {
    fun save(evaluation: InterviewEvaluation): InterviewEvaluation
    fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): InterviewEvaluation?
    fun findAllByApplicantId(applicantId: Long): List<InterviewEvaluation>
    fun findAllByApplicantIdIn(applicantIds: List<Long>): List<InterviewEvaluation>
    fun existsByInterviewEvaluationItemIdIn(itemIds: List<Long>): Boolean
    fun deleteAllByApplicantId(applicantId: Long)
    fun deleteByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long)
    fun existsByApplicantId(applicantId: Long): Boolean
    fun existsByInterviewRubricId(rubricId: Long): Boolean
    fun findRubricIdsByApplicantId(applicantId: Long): List<Long>
}

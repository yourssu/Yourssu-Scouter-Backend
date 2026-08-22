package com.yourssu.scouter.recruiting.evaluation.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class InterviewEvaluationReader(
    private val interviewEvaluationRepository: InterviewEvaluationRepository,
) {
    fun readByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): InterviewEvaluation? {
        return interviewEvaluationRepository.findByApplicantIdAndEvaluatorUserId(applicantId, evaluatorUserId)
    }

    fun readAllByApplicantId(applicantId: Long): List<InterviewEvaluation> {
        return interviewEvaluationRepository.findAllByApplicantId(applicantId)
    }

    fun readAllByApplicantIdIn(applicantIds: List<Long>): List<InterviewEvaluation> {
        return interviewEvaluationRepository.findAllByApplicantIdIn(applicantIds)
    }

    fun existsByInterviewEvaluationItemIdIn(itemIds: List<Long>): Boolean {
        return interviewEvaluationRepository.existsByInterviewEvaluationItemIdIn(itemIds)
    }

    fun existsByApplicantId(applicantId: Long): Boolean {
        return interviewEvaluationRepository.existsByApplicantId(applicantId)
    }

    fun existsByApplicantIdIn(applicantIds: List<Long>): Boolean {
        return interviewEvaluationRepository.existsByApplicantIdIn(applicantIds)
    }
}

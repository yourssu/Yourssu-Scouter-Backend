package com.yourssu.scouter.recruiting.evaluation.storage

import com.yourssu.scouter.recruiting.evaluation.implement.FinalEvaluation
import com.yourssu.scouter.recruiting.evaluation.implement.FinalEvaluationRepository
import com.yourssu.scouter.recruiting.rubric.storage.JpaInterviewRubricRepository
import com.yourssu.scouter.auth.user.storage.JpaUserRepository
import com.yourssu.scouter.recruiting.applicant.storage.JpaApplicantRepository
import org.springframework.stereotype.Repository

@Repository
class FinalEvaluationRepositoryImpl(
    private val jpaFinalEvaluationRepository: JpaFinalEvaluationRepository,
    private val jpaUserRepository: JpaUserRepository,
    private val jpaInterviewRubricRepository: JpaInterviewRubricRepository,
    private val jpaApplicantRepository: JpaApplicantRepository,
) : FinalEvaluationRepository {

    override fun save(finalEvaluation: FinalEvaluation): FinalEvaluation {
        val existing = jpaFinalEvaluationRepository.findByApplicantIdAndUserId(
            finalEvaluation.applicantId,
            finalEvaluation.evaluatorUserId
        )

        val entity = if (existing != null) {
            existing.update(
                overallComment = finalEvaluation.overallComment,
                interviewResult = finalEvaluation.interviewResult,
                score = finalEvaluation.score,
                submit = finalEvaluation.submit,
                submittedAt = finalEvaluation.submittedAt
            )
            jpaFinalEvaluationRepository.save(existing)
        } else {
            val userRef = jpaUserRepository.getReferenceById(finalEvaluation.evaluatorUserId)
            val rubricRef = jpaInterviewRubricRepository.getReferenceById(finalEvaluation.interviewRubricId)
            val applicantRef = jpaApplicantRepository.getReferenceById(finalEvaluation.applicantId)
            val newEntity = FinalEvaluationEntity(
                user = userRef,
                interviewRubric = rubricRef,
                applicant = applicantRef,
                overallComment = finalEvaluation.overallComment,
                interviewResult = finalEvaluation.interviewResult,
                score = finalEvaluation.score,
                submit = finalEvaluation.submit,
                submittedAt = finalEvaluation.submittedAt
            )
            jpaFinalEvaluationRepository.save(newEntity)
        }

        return toDomain(entity)
    }

    override fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): FinalEvaluation? {
        val entity = jpaFinalEvaluationRepository.findByApplicantIdAndUserId(applicantId, evaluatorUserId) ?: return null
        return toDomain(entity)
    }

    override fun findAllByApplicantId(applicantId: Long): List<FinalEvaluation> {
        return jpaFinalEvaluationRepository.findAllByApplicantId(applicantId).map(::toDomain)
    }

    override fun findAllByApplicantIdIn(applicantIds: List<Long>): List<FinalEvaluation> {
        if (applicantIds.isEmpty()) return emptyList()
        return jpaFinalEvaluationRepository.findAllByApplicantIdIn(applicantIds).map(::toDomain)
    }

    override fun deleteAllByApplicantId(applicantId: Long) {
        jpaFinalEvaluationRepository.deleteAllByApplicantId(applicantId)
    }

    private fun toDomain(entity: FinalEvaluationEntity): FinalEvaluation {
        return FinalEvaluation(
            id = entity.id,
            evaluatorUserId = entity.user.id!!,
            interviewRubricId = entity.interviewRubric.id,
            applicantId = entity.applicant.id!!,
            overallComment = entity.overallComment ?: "",
            interviewResult = entity.interviewResult,
            score = entity.score,
            submit = entity.submit,
            submittedAt = entity.submittedAt
        )
    }
}

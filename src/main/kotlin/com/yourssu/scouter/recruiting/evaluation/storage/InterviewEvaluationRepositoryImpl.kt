package com.yourssu.scouter.recruiting.evaluation.storage

import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluation
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationRepository
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationScoreItem
import org.springframework.stereotype.Repository

@Repository
class InterviewEvaluationRepositoryImpl(
    private val jpaInterviewEvaluationRepository: JpaInterviewEvaluationRepository,
    private val jpaInterviewEvaluationItemRepository: JpaInterviewEvaluationItemRepository,
) : InterviewEvaluationRepository {

    override fun save(evaluation: InterviewEvaluation): InterviewEvaluation {
        val existingEntities = jpaInterviewEvaluationRepository.findAllByApplicantIdAndMemberId(
            evaluation.applicantId,
            evaluation.evaluatorUserId
        ).associateBy { it.interviewEvaluationItem.id }

        val savedEntities = evaluation.items.map { item ->
            val entity = existingEntities[item.evaluationItemId] ?: InterviewEvaluationEntity(
                applicantId = evaluation.applicantId,
                interviewEvaluationItem = jpaInterviewEvaluationItemRepository.getReferenceById(item.evaluationItemId),
                memberId = evaluation.evaluatorUserId,
                score = item.score,
                result = evaluation.result,
                submittedAt = evaluation.submittedAt
            )
            entity.updateEvaluation(item.score, evaluation.result, evaluation.submittedAt)
            jpaInterviewEvaluationRepository.save(entity)
        }

        // Delete any items that are no longer present in the updated list
        val updatedItemIds = evaluation.items.map { it.evaluationItemId }.toSet()
        existingEntities.values.filter { it.interviewEvaluationItem.id !in updatedItemIds }.forEach {
            jpaInterviewEvaluationRepository.delete(it)
        }

        return toDomain(evaluation.applicantId, evaluation.evaluatorUserId, savedEntities)
    }

    override fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): InterviewEvaluation? {
        val entities = jpaInterviewEvaluationRepository.findAllByApplicantIdAndMemberId(applicantId, evaluatorUserId)
        if (entities.isEmpty()) {
            return null
        }
        return toDomain(applicantId, evaluatorUserId, entities)
    }

    override fun findAllByApplicantId(applicantId: Long): List<InterviewEvaluation> {
        val entities = jpaInterviewEvaluationRepository.findAllByApplicantId(applicantId)
        return entities.groupBy { it.memberId }.map { (evaluatorUserId, userEntities) ->
            toDomain(applicantId, evaluatorUserId, userEntities)
        }
    }

    override fun findAllByApplicantIdIn(applicantIds: List<Long>): List<InterviewEvaluation> {
        if (applicantIds.isEmpty()) {
            return emptyList()
        }
        val entities = jpaInterviewEvaluationRepository.findAllByApplicantIdIn(applicantIds)
        return entities.groupBy { it.applicantId to it.memberId }.map { (key, groupEntities) ->
            val (applicantId, evaluatorUserId) = key
            toDomain(applicantId, evaluatorUserId, groupEntities)
        }
    }

    private fun toDomain(
        applicantId: Long,
        evaluatorUserId: Long,
        entities: List<InterviewEvaluationEntity>
    ): InterviewEvaluation {
        val first = entities.first()
        return InterviewEvaluation(
            id = first.id, // Using first entity's ID or similar reference
            applicantId = applicantId,
            evaluatorUserId = evaluatorUserId,
            items = entities.map { entity ->
                InterviewEvaluationScoreItem(
                    id = entity.id,
                    evaluationItemId = entity.interviewEvaluationItem.id,
                    score = entity.score
                )
            },
            result = first.result,
            submittedAt = first.submittedAt
        )
    }
}

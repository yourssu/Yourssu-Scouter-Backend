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
        val existingEntities = jpaInterviewEvaluationRepository.findAllByApplicantIdAndUserId(
            evaluation.applicantId,
            evaluation.evaluatorUserId
        ).associateBy { it.interviewEvaluationItem.id }

        val savedEntities = evaluation.items.map { item ->
            val entity = existingEntities[item.evaluationItemId] ?: InterviewEvaluationEntity(
                applicantId = evaluation.applicantId,
                interviewEvaluationItem = jpaInterviewEvaluationItemRepository.getReferenceById(item.evaluationItemId),
                userId = evaluation.evaluatorUserId,
                score = item.score
            )
            entity.updateEvaluation(item.score)
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
        val entities = jpaInterviewEvaluationRepository.findAllByApplicantIdAndUserId(applicantId, evaluatorUserId)
        if (entities.isEmpty()) {
            return null
        }
        return toDomain(applicantId, evaluatorUserId, entities)
    }

    override fun findAllByApplicantId(applicantId: Long): List<InterviewEvaluation> {
        val entities = jpaInterviewEvaluationRepository.findAllByApplicantId(applicantId)
        return entities.groupBy { it.userId }.map { (evaluatorUserId, userEntities) ->
            toDomain(applicantId, evaluatorUserId, userEntities)
        }
    }

    override fun findAllByApplicantIdIn(applicantIds: List<Long>): List<InterviewEvaluation> {
        if (applicantIds.isEmpty()) {
            return emptyList()
        }
        val entities = jpaInterviewEvaluationRepository.findAllByApplicantIdIn(applicantIds)
        return entities.groupBy { it.applicantId to it.userId }.map { (key, groupEntities) ->
            val (applicantId, evaluatorUserId) = key
            toDomain(applicantId, evaluatorUserId, groupEntities)
        }
    }

    override fun existsByInterviewEvaluationItemIdIn(itemIds: List<Long>): Boolean {
        if (itemIds.isEmpty()) {
            return false
        }
        return jpaInterviewEvaluationRepository.existsByInterviewEvaluationItemIdIn(itemIds)
    }

    override fun deleteAllByApplicantId(applicantId: Long) {
        jpaInterviewEvaluationRepository.deleteAllByApplicantId(applicantId)
    }

    override fun existsByApplicantId(applicantId: Long): Boolean {
        return jpaInterviewEvaluationRepository.existsByApplicantId(applicantId)
    }

    override fun existsByApplicantIdIn(applicantIds: List<Long>): Boolean {
        if (applicantIds.isEmpty()) return false
        return jpaInterviewEvaluationRepository.existsByApplicantIdIn(applicantIds)
    }

    private fun toDomain(
        applicantId: Long,
        evaluatorUserId: Long,
        entities: List<InterviewEvaluationEntity>
    ): InterviewEvaluation {
        val first = entities.firstOrNull()
        return InterviewEvaluation(
            id = first?.id,
            applicantId = applicantId,
            evaluatorUserId = evaluatorUserId,
            items = entities.map { entity ->
                InterviewEvaluationScoreItem(
                    id = entity.id,
                    evaluationItemId = entity.interviewEvaluationItem.id,
                    score = entity.score
                )
            }
        )
    }
}


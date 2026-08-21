package com.yourssu.scouter.recruiting.evaluation.storage

import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluation
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluationRepository
import org.springframework.stereotype.Repository

@Repository
class DocumentEvaluationRepositoryImpl(
    private val jpaDocumentEvaluationRepository: JpaDocumentEvaluationRepository,
    private val jpaDocumentEvaluationItemRepository: JpaDocumentEvaluationItemRepository,
) : DocumentEvaluationRepository {

    override fun save(evaluation: DocumentEvaluation): DocumentEvaluation {
        val existingId = evaluation.id
            ?: jpaDocumentEvaluationRepository
                .findByApplicantIdAndEvaluatorUserId(evaluation.applicantId, evaluation.evaluatorUserId)?.id

        val entity = DocumentEvaluationEntity(
            id = existingId,
            applicantId = evaluation.applicantId,
            evaluatorUserId = evaluation.evaluatorUserId,
            overallComment = evaluation.overallComment,
            result = evaluation.result,
            submittedAt = evaluation.submittedAt,
        )
        val savedEntity = jpaDocumentEvaluationRepository.save(entity)

        if (existingId != null) {
            jpaDocumentEvaluationItemRepository.deleteAllByEvaluationId(existingId)
        }

        val itemEntities = evaluation.items.map { item ->
            DocumentEvaluationItemEntity(
                evaluation = savedEntity,
                sectionId = item.sectionId,
                score = item.score,
                memo = item.memo,
            )
        }
        val savedItems = jpaDocumentEvaluationItemRepository.saveAll(itemEntities)

        return savedEntity.toDomain(savedItems.map { it.toDomain() })
    }

    override fun findByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long): DocumentEvaluation? {
        val entity = jpaDocumentEvaluationRepository
            .findByApplicantIdAndEvaluatorUserId(applicantId, evaluatorUserId) ?: return null
        val items = jpaDocumentEvaluationItemRepository.findAllByEvaluationId(entity.id!!)

        return entity.toDomain(items.map { it.toDomain() })
    }

    override fun findAllByApplicantId(applicantId: Long): List<DocumentEvaluation> {
        val entities = jpaDocumentEvaluationRepository.findAllByApplicantId(applicantId)
        val itemsByEvaluationId = jpaDocumentEvaluationItemRepository
            .findAllByEvaluationIdIn(entities.mapNotNull { it.id })
            .groupBy { it.evaluation.id }

        return entities.map { entity ->
            entity.toDomain((itemsByEvaluationId[entity.id] ?: emptyList()).map { it.toDomain() })
        }
    }

    override fun findAllByApplicantIdIn(applicantIds: List<Long>): List<DocumentEvaluation> {
        if (applicantIds.isEmpty()) {
            return emptyList()
        }

        val entities = jpaDocumentEvaluationRepository.findAllByApplicantIdIn(applicantIds)
        val itemsByEvaluationId = jpaDocumentEvaluationItemRepository
            .findAllByEvaluationIdIn(entities.mapNotNull { it.id })
            .groupBy { it.evaluation.id }

        return entities.map { entity ->
            entity.toDomain((itemsByEvaluationId[entity.id] ?: emptyList()).map { it.toDomain() })
        }
    }

    override fun existsBySectionIdIn(sectionIds: List<Long>): Boolean {
        return jpaDocumentEvaluationItemRepository.existsBySectionIdIn(sectionIds)
    }

    override fun deleteAllByApplicantId(applicantId: Long) {
        val evaluationIds = jpaDocumentEvaluationRepository.findAllByApplicantId(applicantId).mapNotNull { it.id }
        if (evaluationIds.isEmpty()) {
            return
        }

        jpaDocumentEvaluationItemRepository.deleteAllByEvaluationIdIn(evaluationIds)
        jpaDocumentEvaluationRepository.deleteAllByApplicantId(applicantId)
    }
}

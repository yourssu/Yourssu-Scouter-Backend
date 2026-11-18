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

    override fun findAverageSubmittedTotalScoreByApplicantIds(applicantIds: Collection<Long>): Map<Long, Double> {
        if (applicantIds.isEmpty()) {
            return emptyMap()
        }

        return jpaDocumentEvaluationRepository.sumSubmittedScoresByApplicantIds(applicantIds)
            .associate { it.applicantId to (it.scoreSum ?: 0L).toDouble() / it.evaluationCount }
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

    override fun deleteByApplicantIdAndEvaluatorUserId(applicantId: Long, evaluatorUserId: Long) {
        val entity = jpaDocumentEvaluationRepository
            .findByApplicantIdAndEvaluatorUserId(applicantId, evaluatorUserId) ?: return

        // 벌크 삭제 쿼리는 영속성 컨텍스트에 이미 올라온 문항 엔티티를 치우지 못해, 부모를 지운 뒤 flush 에서 터진다.
        jpaDocumentEvaluationItemRepository
            .deleteAll(jpaDocumentEvaluationItemRepository.findAllByEvaluationId(entity.id!!))
        jpaDocumentEvaluationRepository.delete(entity)
    }
}

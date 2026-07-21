package com.yourssu.scouter.recruiting.evaluation.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface JpaDocumentEvaluationItemRepository : JpaRepository<DocumentEvaluationItemEntity, Long> {
    @Modifying
    @Query("DELETE FROM DocumentEvaluationItemEntity i WHERE i.evaluation.id = :evaluationId")
    fun deleteAllByEvaluationId(evaluationId: Long)

    fun findAllByEvaluationId(evaluationId: Long): List<DocumentEvaluationItemEntity>

    fun findAllByEvaluationIdIn(evaluationIds: List<Long>): List<DocumentEvaluationItemEntity>

    fun existsBySectionIdIn(sectionIds: List<Long>): Boolean
}

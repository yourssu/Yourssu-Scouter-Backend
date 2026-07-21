package com.yourssu.scouter.recruiting.deadline.storage

import com.yourssu.scouter.recruiting.deadline.implement.PartDocumentDeadline
import com.yourssu.scouter.recruiting.deadline.implement.PartDocumentDeadlineRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class PartDocumentDeadlineRepositoryImpl(
    private val jpaPartDocumentDeadlineRepository: JpaPartDocumentDeadlineRepository,
) : PartDocumentDeadlineRepository {

    override fun findByPartId(partId: Long): PartDocumentDeadline? {
        return jpaPartDocumentDeadlineRepository.findByIdOrNull(partId)?.toDomain()
    }

    override fun upsert(deadline: PartDocumentDeadline): PartDocumentDeadline {
        val entity = PartDocumentDeadlineEntity(partId = deadline.partId, deadline = deadline.deadline)

        return jpaPartDocumentDeadlineRepository.save(entity).toDomain()
    }
}

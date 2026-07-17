package com.yourssu.scouter.document.storage.domain.deadline

import com.yourssu.scouter.document.implement.domain.deadline.PartDocumentDeadline
import com.yourssu.scouter.document.implement.domain.deadline.PartDocumentDeadlineRepository
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

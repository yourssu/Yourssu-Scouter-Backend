package com.yourssu.scouter.document.implement.domain.deadline

interface PartDocumentDeadlineRepository {

    fun findByPartId(partId: Long): PartDocumentDeadline?

    fun upsert(deadline: PartDocumentDeadline): PartDocumentDeadline
}

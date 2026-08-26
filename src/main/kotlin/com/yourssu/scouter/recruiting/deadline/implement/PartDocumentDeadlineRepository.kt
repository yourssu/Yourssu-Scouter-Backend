package com.yourssu.scouter.recruiting.deadline.implement

interface PartDocumentDeadlineRepository {

    fun findByPartId(partId: Long): PartDocumentDeadline?

    fun upsert(deadline: PartDocumentDeadline): PartDocumentDeadline
}

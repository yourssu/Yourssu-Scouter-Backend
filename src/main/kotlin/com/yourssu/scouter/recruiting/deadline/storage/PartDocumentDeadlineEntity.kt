package com.yourssu.scouter.recruiting.deadline.storage

import com.yourssu.scouter.recruiting.deadline.implement.PartDocumentDeadline
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "part_document_deadline")
class PartDocumentDeadlineEntity(

    @Id
    @Column(name = "part_id")
    val partId: Long,

    @Column(nullable = false)
    val deadline: Instant,
) {

    fun toDomain(): PartDocumentDeadline = PartDocumentDeadline(
        partId = partId,
        deadline = deadline,
    )
}

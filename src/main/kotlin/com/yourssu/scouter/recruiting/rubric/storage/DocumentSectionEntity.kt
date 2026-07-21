package com.yourssu.scouter.recruiting.rubric.storage

import com.yourssu.scouter.common.part.storage.PartEntity
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSection
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "document_section")
class DocumentSectionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "part_id", nullable = false, foreignKey = ForeignKey(name = "fk_document_section_part"))
    val part: PartEntity,

    @Column(nullable = false, columnDefinition = "TEXT")
    val question: String,

    @Column(nullable = false)
    val maxScore: Int,

    @Column(nullable = false)
    val criterionDetail: String,
) {

    fun toDomain(): DocumentSection = DocumentSection(
        id = id,
        partId = part.id!!,
        question = question,
        maxScore = maxScore,
        criterionDetail = criterionDetail,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentSectionEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

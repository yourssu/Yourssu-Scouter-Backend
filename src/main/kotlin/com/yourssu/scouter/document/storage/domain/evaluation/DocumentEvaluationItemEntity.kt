package com.yourssu.scouter.document.storage.domain.evaluation

import com.yourssu.scouter.document.implement.domain.evaluation.DocumentEvaluationItem
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
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(name = "document_evaluation_item")
class DocumentEvaluationItemEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false, foreignKey = ForeignKey(name = "fk_document_evaluation_item_evaluation"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    val evaluation: DocumentEvaluationEntity,

    @Column(name = "section_id", nullable = false)
    val sectionId: Long,

    @Column(nullable = false)
    val score: Int,

    @Column(nullable = false, columnDefinition = "TEXT")
    val memo: String,
) {

    fun toDomain(): DocumentEvaluationItem = DocumentEvaluationItem(
        sectionId = sectionId,
        score = score,
        memo = memo,
    )
}

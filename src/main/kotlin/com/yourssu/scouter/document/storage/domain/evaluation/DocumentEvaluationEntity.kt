package com.yourssu.scouter.document.storage.domain.evaluation

import com.yourssu.scouter.document.implement.domain.evaluation.DocumentEvaluation
import com.yourssu.scouter.document.implement.domain.evaluation.DocumentEvaluationItem
import com.yourssu.scouter.document.implement.domain.evaluation.DocumentResult
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "document_evaluation",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_document_evaluation_applicant_evaluator",
            columnNames = ["applicant_id", "evaluator_user_id"],
        ),
    ],
)
class DocumentEvaluationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "applicant_id", nullable = false)
    val applicantId: Long,

    @Column(name = "evaluator_user_id", nullable = false)
    val evaluatorUserId: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    val overallComment: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val result: DocumentResult,

    val submittedAt: Instant? = null,
) {

    fun toDomain(items: List<DocumentEvaluationItem>): DocumentEvaluation = DocumentEvaluation(
        id = id,
        applicantId = applicantId,
        evaluatorUserId = evaluatorUserId,
        items = items,
        overallComment = overallComment,
        result = result,
        submittedAt = submittedAt,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentEvaluationEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

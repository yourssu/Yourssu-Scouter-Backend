package com.yourssu.scouter.document.storage.domain.comment

import com.yourssu.scouter.document.implement.domain.comment.DocumentComment
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import jakarta.persistence.EntityListeners
import java.time.Instant

@Entity
@Table(name = "document_comment")
@EntityListeners(AuditingEntityListener::class)
class DocumentCommentEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "applicant_id", nullable = false)
    val applicantId: Long,

    @Column(name = "section_id", nullable = false)
    val sectionId: Long,

    @Column(name = "author_user_id", nullable = false)
    val authorUserId: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,
) {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null
        protected set

    fun toDomain(): DocumentComment = DocumentComment(
        id = id,
        applicantId = applicantId,
        sectionId = sectionId,
        authorUserId = authorUserId,
        content = content,
        createdAt = createdAt,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentCommentEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

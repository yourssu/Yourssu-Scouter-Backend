package com.yourssu.scouter.recruiting.comment.storage

import com.yourssu.scouter.recruiting.comment.implement.DocumentComment
import com.yourssu.scouter.recruiting.comment.implement.DocumentCommentRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class DocumentCommentRepositoryImpl(
    private val jpaDocumentCommentRepository: JpaDocumentCommentRepository,
) : DocumentCommentRepository {

    override fun save(comment: DocumentComment): DocumentComment {
        val entity = DocumentCommentEntity(
            id = comment.id,
            applicantId = comment.applicantId,
            sectionId = comment.sectionId,
            authorUserId = comment.authorUserId,
            content = comment.content,
            parentCommentId = comment.parentCommentId,
        )

        return jpaDocumentCommentRepository.save(entity).toDomain()
    }

    override fun findAllByApplicantId(applicantId: Long): List<DocumentComment> {
        return jpaDocumentCommentRepository.findAllByApplicantId(applicantId).map { it.toDomain() }
    }

    override fun findById(commentId: Long): DocumentComment? {
        return jpaDocumentCommentRepository.findByIdOrNull(commentId)?.toDomain()
    }

    override fun deleteById(commentId: Long) {
        jpaDocumentCommentRepository.deleteById(commentId)
    }
}

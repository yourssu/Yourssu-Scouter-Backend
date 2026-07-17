package com.yourssu.scouter.document.storage.domain.comment

import com.yourssu.scouter.document.implement.domain.comment.DocumentComment
import com.yourssu.scouter.document.implement.domain.comment.DocumentCommentRepository
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

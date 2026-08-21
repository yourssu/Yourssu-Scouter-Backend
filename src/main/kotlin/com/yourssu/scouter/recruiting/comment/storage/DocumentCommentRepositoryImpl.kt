package com.yourssu.scouter.recruiting.comment.storage

import com.yourssu.scouter.recruiting.comment.implement.CommentCategory
import com.yourssu.scouter.recruiting.comment.implement.DocumentComment
import com.yourssu.scouter.recruiting.comment.implement.DocumentCommentRepository
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentCommentNotFoundException
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
            category = comment.category,
            isEdited = comment.isEdited,
        )

        return jpaDocumentCommentRepository.save(entity).toDomain()
    }

    override fun update(comment: DocumentComment): DocumentComment {
        val entity = jpaDocumentCommentRepository.findByIdOrNull(comment.id)
            ?: throw DocumentCommentNotFoundException("지정한 코멘트를 찾을 수 없습니다.")

        entity.content = comment.content
        entity.isEdited = comment.isEdited

        return jpaDocumentCommentRepository.save(entity).toDomain()
    }

    override fun findAllByApplicantIdAndCategoryOrderByCreatedAtAsc(applicantId: Long, category: CommentCategory): List<DocumentComment> {
        return jpaDocumentCommentRepository.findAllByApplicantIdAndCategoryOrderByCreatedAtAsc(applicantId, category).map { it.toDomain() }
    }

    override fun findById(commentId: Long): DocumentComment? {
        return jpaDocumentCommentRepository.findByIdOrNull(commentId)?.toDomain()
    }

    override fun deleteById(commentId: Long) {
        jpaDocumentCommentRepository.deleteById(commentId)
    }
}

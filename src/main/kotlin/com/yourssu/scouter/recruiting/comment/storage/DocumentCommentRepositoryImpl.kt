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

    override fun deleteAllByApplicantId(applicantId: Long) {
        val comments = jpaDocumentCommentRepository.findAllByApplicantId(applicantId)
        if (comments.isEmpty()) {
            return
        }

        // parent_comment_id 는 document_comment 자기 자신을 ON DELETE CASCADE 로 참조한다(V23).
        // 부모를 먼저 지우면 DB 가 답글까지 지워 버려서, 뒤이은 답글 삭제가 0건이 되고 flush 에서 터진다.
        // 답글은 항상 부모보다 나중에 생기므로 id 내림차순이면 자식이 먼저 사라진다.
        jpaDocumentCommentRepository.deleteAll(comments.sortedByDescending { it.id })
    }
}

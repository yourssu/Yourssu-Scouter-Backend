package com.yourssu.scouter.document.implement.domain.comment

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class DocumentCommentWriter(
    private val documentCommentRepository: DocumentCommentRepository,
) {

    fun write(comment: DocumentComment): DocumentComment {
        return documentCommentRepository.save(comment)
    }

    fun delete(commentId: Long) {
        documentCommentRepository.deleteById(commentId)
    }
}

package com.yourssu.scouter.recruiting.comment.implement

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

    fun update(comment: DocumentComment): DocumentComment {
        return documentCommentRepository.update(comment)
    }

    fun delete(commentId: Long) {
        documentCommentRepository.deleteById(commentId)
    }
}

package com.yourssu.scouter.recruiting.comment.implement

import com.yourssu.scouter.recruiting.support.implement.exception.DocumentCommentNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class DocumentCommentReader(
    private val documentCommentRepository: DocumentCommentRepository,
) {

    fun readAllByApplicantId(applicantId: Long): List<DocumentComment> {
        return documentCommentRepository.findAllByApplicantId(applicantId)
    }

    fun readById(commentId: Long): DocumentComment {
        return documentCommentRepository.findById(commentId)
            ?: throw DocumentCommentNotFoundException("지정한 코멘트를 찾을 수 없습니다.")
    }
}

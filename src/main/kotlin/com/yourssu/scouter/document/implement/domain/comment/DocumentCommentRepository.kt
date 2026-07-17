package com.yourssu.scouter.document.implement.domain.comment

interface DocumentCommentRepository {

    fun save(comment: DocumentComment): DocumentComment

    fun findAllByApplicantId(applicantId: Long): List<DocumentComment>

    fun findById(commentId: Long): DocumentComment?

    fun deleteById(commentId: Long)
}

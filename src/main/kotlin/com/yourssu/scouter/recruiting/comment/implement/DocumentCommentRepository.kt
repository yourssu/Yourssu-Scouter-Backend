package com.yourssu.scouter.recruiting.comment.implement

interface DocumentCommentRepository {

    fun save(comment: DocumentComment): DocumentComment

    fun findAllByApplicantId(applicantId: Long): List<DocumentComment>

    fun findById(commentId: Long): DocumentComment?

    fun deleteById(commentId: Long)
}

package com.yourssu.scouter.recruiting.comment.implement

import com.yourssu.scouter.recruiting.comment.implement.CommentCategory

interface DocumentCommentRepository {

    fun save(comment: DocumentComment): DocumentComment

    fun update(comment: DocumentComment): DocumentComment

    fun findAllByApplicantIdAndCategory(applicantId: Long, category: CommentCategory): List<DocumentComment>

    fun findById(commentId: Long): DocumentComment?

    fun deleteById(commentId: Long)
}

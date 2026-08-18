package com.yourssu.scouter.recruiting.comment.storage

import com.yourssu.scouter.recruiting.comment.implement.CommentCategory
import org.springframework.data.jpa.repository.JpaRepository

interface JpaDocumentCommentRepository : JpaRepository<DocumentCommentEntity, Long> {

    fun findAllByApplicantIdAndCategory(applicantId: Long, category: CommentCategory): List<DocumentCommentEntity>
}

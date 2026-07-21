package com.yourssu.scouter.recruiting.comment.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaDocumentCommentRepository : JpaRepository<DocumentCommentEntity, Long> {

    fun findAllByApplicantId(applicantId: Long): List<DocumentCommentEntity>
}

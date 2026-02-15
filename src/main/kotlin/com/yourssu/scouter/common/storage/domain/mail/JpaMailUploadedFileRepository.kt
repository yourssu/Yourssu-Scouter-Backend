package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileStatus
import org.springframework.data.jpa.repository.JpaRepository

interface JpaMailUploadedFileRepository : JpaRepository<MailUploadedFileEntity, Long> {
    fun findAllByUserIdAndStatus(
        userId: Long,
        status: MailUploadedFileStatus,
    ): List<MailUploadedFileEntity>
}

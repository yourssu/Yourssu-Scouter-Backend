package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailFileUsage
import com.yourssu.scouter.common.implement.domain.mail.MailUploadedFileStatus
import org.springframework.data.jpa.repository.JpaRepository

interface JpaMailUploadedFileRepository : JpaRepository<MailUploadedFileEntity, Long> {
    fun findAllByUserIdAndStatus(
        userId: Long,
        status: MailUploadedFileStatus,
    ): List<MailUploadedFileEntity>

    fun findAllByIdIn(ids: List<Long>): List<MailUploadedFileEntity>

    fun findAllByUserIdAndStatusAndUsage(
        userId: Long,
        status: MailUploadedFileStatus,
        usage: MailFileUsage,
    ): List<MailUploadedFileEntity>
}

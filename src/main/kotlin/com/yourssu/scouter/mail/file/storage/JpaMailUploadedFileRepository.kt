package com.yourssu.scouter.mail.file.storage

import com.yourssu.scouter.mail.file.implement.MailFileUsage
import com.yourssu.scouter.mail.file.implement.MailUploadedFileStatus
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

    fun findByStorageKeyAndStatus(
        storageKey: String,
        status: MailUploadedFileStatus,
    ): MailUploadedFileEntity?
}

package com.yourssu.scouter.common.implement.domain.mail

import java.time.Instant

enum class MailUploadedFileStatus {
    ACTIVE,
    DELETED,
}

data class MailUploadedFile(
    val id: Long? = null,
    val userId: Long,
    val usage: MailFileUsage,
    val fileName: String,
    val contentType: String,
    val storageKey: String,
    val status: MailUploadedFileStatus = MailUploadedFileStatus.ACTIVE,
    val used: Boolean = false,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

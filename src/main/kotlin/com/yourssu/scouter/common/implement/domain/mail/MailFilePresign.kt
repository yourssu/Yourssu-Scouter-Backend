package com.yourssu.scouter.common.implement.domain.mail

import java.time.Instant

enum class MailFileUsage {
    INLINE,
    ATTACHMENT,
}

data class MailFilePresignResult(
    val s3Key: String,
    val putUrl: String,
    val expiresAt: Instant,
    val contentType: String,
)

package com.yourssu.scouter.mail.file.implement

import java.time.Instant

enum class MailFileUsage {
    INLINE,
    ATTACHMENT,
}

data class MailFilePresignResult(
    val cid: String,
    val putUrl: String,
    val expiresAt: Instant,
    val contentType: String,
)

data class MailFileDownloadResult(
    val getUrl: String,
    val expiresAt: Instant,
)

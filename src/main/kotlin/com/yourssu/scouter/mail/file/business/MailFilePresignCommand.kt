package com.yourssu.scouter.mail.file.business

import com.yourssu.scouter.mail.file.implement.MailFileUsage

data class MailFilePresignCommand(
    val userId: Long,
    val fileName: String,
    val contentType: String,
    val usage: MailFileUsage,
)

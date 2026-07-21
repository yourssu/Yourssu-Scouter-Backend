package com.yourssu.scouter.mail.business.dto

import com.yourssu.scouter.mail.implement.file.MailFileUsage

data class MailFilePresignCommand(
    val userId: Long,
    val fileName: String,
    val contentType: String,
    val usage: MailFileUsage,
)

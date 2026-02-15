package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailFileUsage

data class MailFilePresignCommand(
    val userId: Long,
    val fileName: String,
    val contentType: String,
    val usage: MailFileUsage,
)

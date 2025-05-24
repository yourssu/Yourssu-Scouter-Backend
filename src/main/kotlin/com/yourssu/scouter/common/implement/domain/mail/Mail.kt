package com.yourssu.scouter.common.implement.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import jakarta.mail.util.ByteArrayDataSource

class Mail(
    val id: Long? = null,
    val senderEmailAddress: String,
    val receiverEmailAddresses: List<String>,
    val ccEmailAddresses: List<String> = emptyList(),
    val bccEmailAddresses: List<String> = emptyList(),
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val inlineImages: Map<String, ByteArrayDataSource> = emptyMap(),
    val attachments: Map<String, ByteArrayDataSource> = emptyMap(),
)

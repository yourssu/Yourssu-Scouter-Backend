package com.yourssu.scouter.common.business.domain.mail

import jakarta.mail.util.ByteArrayDataSource

data class MailData(
    val senderEmailAddress: String,
    val receiverEmailAddresses: List<String> = emptyList(),
    val ccEmailAddresses: List<String> = emptyList(),
    val bccEmailAddresses: List<String> = emptyList(),
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val inlineImages: Map<String, ByteArrayDataSource> = emptyMap(),
    val attachments: Map<String, ByteArrayDataSource> = emptyMap(),
)

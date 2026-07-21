package com.yourssu.scouter.mail.business.dto

import com.yourssu.scouter.mail.business.MailBodyFormat

import com.yourssu.scouter.mail.implement.message.MailAttachmentReference

data class MailSendCommand(
    val senderUserId: Long,
    val receiverEmailAddresses: List<String>,
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val attachmentReferences: List<MailAttachmentReference> = emptyList(),
)

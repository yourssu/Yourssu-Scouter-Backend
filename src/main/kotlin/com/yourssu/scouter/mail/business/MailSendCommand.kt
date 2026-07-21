package com.yourssu.scouter.mail.business

import com.yourssu.scouter.mail.implement.message.MailAttachmentReference

data class MailSendCommand(
    val senderUserId: Long,
    val receiverEmailAddresses: List<String>,
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val attachmentReferences: List<MailAttachmentReference> = emptyList(),
)

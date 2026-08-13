package com.yourssu.scouter.mail.core.business

import com.yourssu.scouter.mail.core.implement.MailAttachmentReference

data class MailSendCommand(
    val senderUserId: Long,
    val receiverEmailAddresses: List<String>,
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val attachmentReferences: List<MailAttachmentReference> = emptyList(),
)

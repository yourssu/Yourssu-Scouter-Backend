package com.yourssu.scouter.common.implement.domain.mail.message

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import jakarta.mail.util.ByteArrayDataSource

class Mail(
    val receiverEmailAddress: String,
    val ccEmailAddresses: List<String> = emptyList(),
    val bccEmailAddresses: List<String> = emptyList(),
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val attachmentReferences: List<MailAttachmentReference> = emptyList(),
)

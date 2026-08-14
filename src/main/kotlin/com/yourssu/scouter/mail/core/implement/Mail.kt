package com.yourssu.scouter.mail.core.implement

import com.yourssu.scouter.mail.core.business.MailBodyFormat

class Mail(
    val receiverEmailAddress: String,
    val ccEmailAddresses: List<String> = emptyList(),
    val bccEmailAddresses: List<String> = emptyList(),
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val attachmentReferences: List<MailAttachmentReference> = emptyList(),
)

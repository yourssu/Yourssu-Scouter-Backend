package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.Mail
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
) {
    companion object {
        fun from(mail: Mail): MailData {
            return MailData(
                senderEmailAddress = mail.senderEmailAddress,
                receiverEmailAddresses = mail.receiverEmailAddresses,
                ccEmailAddresses = mail.ccEmailAddresses,
                bccEmailAddresses = mail.bccEmailAddresses,
                mailSubject = mail.mailSubject,
                mailBody = mail.mailBody,
                bodyFormat = mail.bodyFormat,
                inlineImages = mail.inlineImages,
                attachments = mail.attachments
            )
        }
    }
}

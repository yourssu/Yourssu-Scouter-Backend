package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.Mail
import com.yourssu.scouter.common.implement.domain.mail.MailAttachmentReference
import java.time.Instant

data class MailReserveCommand(
    val senderUserId: Long,
    val receiverEmailAddresses: List<String>,
    val ccEmailAddresses: List<String> = emptyList(),
    val bccEmailAddresses: List<String> = emptyList(),
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val attachmentReferences: List<MailAttachmentReference> = emptyList(),
    val reservationTime: Instant,
) {
    fun toMail(senderEmailAddress: String): Mail {
        return Mail(
            senderEmailAddress = senderEmailAddress,
            receiverEmailAddresses = receiverEmailAddresses,
            ccEmailAddresses = ccEmailAddresses,
            bccEmailAddresses = bccEmailAddresses,
            mailSubject = mailSubject,
            mailBody = mailBody,
            bodyFormat = bodyFormat,
            attachmentReferences = attachmentReferences,
        )
    }
}

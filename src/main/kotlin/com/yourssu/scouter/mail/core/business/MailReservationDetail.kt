package com.yourssu.scouter.mail.core.business

import com.yourssu.scouter.mail.core.implement.MailAttachmentReference
import com.yourssu.scouter.mail.core.implement.MailReservationStatus
import java.time.Instant

data class MailReservationDetail(
    val reservationId: Long,
    val reservationTime: Instant,
    val status: MailReservationStatus,
    val senderEmailAddress: String?,
    val receiverEmailAddresses: List<String>,
    val ccEmailAddresses: List<String>,
    val bccEmailAddresses: List<String>,
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val attachmentReferences: List<MailAttachmentReference>,
)

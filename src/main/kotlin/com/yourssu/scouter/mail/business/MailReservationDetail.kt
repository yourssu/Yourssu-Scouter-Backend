package com.yourssu.scouter.mail.business

import com.yourssu.scouter.mail.implement.message.MailAttachmentReference
import com.yourssu.scouter.mail.implement.reservation.MailReservationStatus
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

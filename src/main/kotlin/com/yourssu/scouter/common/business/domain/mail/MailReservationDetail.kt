package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailAttachmentReference
import com.yourssu.scouter.common.implement.domain.mail.MailReservationStatus
import java.time.Instant

data class MailReservationDetail(
    val reservationId: Long,
    val mailId: Long,
    val reservationTime: Instant,
    val status: MailReservationStatus,
    val senderEmailAddress: String,
    val receiverEmailAddresses: List<String>,
    val ccEmailAddresses: List<String>,
    val bccEmailAddresses: List<String>,
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val attachmentReferences: List<MailAttachmentReference>
)


package com.yourssu.scouter.common.implement.domain.mail.reservation

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import com.yourssu.scouter.common.implement.domain.mail.message.MailAttachmentReference
import java.time.Instant

data class MailReservation(
    val id: Long? = null,
    val senderEmailAddress: String,
    val receiverEmailAddress: String,
    val ccEmailAddresses: List<String> = emptyList(),
    val bccEmailAddresses: List<String> = emptyList(),
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val attachmentReferences: List<MailAttachmentReference> = emptyList(),
    val groupId: Long? = null,
    val reservationTime: Instant,
    val status: MailReservationStatus = MailReservationStatus.SCHEDULED,
    val claimedAt: Instant? = null,
) {
    fun canRetry(now: Instant): Boolean {
        return status != MailReservationStatus.SENT && reservationTime.isBefore(now)
    }

    fun canCancel(now: Instant): Boolean {
        return status != MailReservationStatus.SENT &&
            status != MailReservationStatus.SENDING &&
            reservationTime.isAfter(now)
    }

    fun canEdit(now: Instant): Boolean {
        return canCancel(now)
    }

    fun markSent(): MailReservation {
        return copy(status = MailReservationStatus.SENT)
    }

    fun markPendingSend(): MailReservation {
        return copy(status = MailReservationStatus.PENDING_SEND)
    }
}

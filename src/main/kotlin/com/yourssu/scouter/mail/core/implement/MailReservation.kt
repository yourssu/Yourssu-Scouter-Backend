package com.yourssu.scouter.mail.core.implement

import com.yourssu.scouter.mail.core.business.MailBodyFormat
import java.time.Instant

data class MailReservation(
    val id: Long? = null,
    // 예약자 users.id (null: 레거시 backfill 실패 데이터)
    val reservedByUserId: Long?,
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

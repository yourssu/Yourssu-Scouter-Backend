package com.yourssu.scouter.common.implement.domain.mail.message

import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservation
import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import jakarta.mail.util.ByteArrayDataSource
import java.time.Instant

class Mail(
    val id: Long? = null,
    val senderEmailAddress: String,
    val receiverEmailAddress: String,
    val ccEmailAddresses: List<String> = emptyList(),
    val bccEmailAddresses: List<String> = emptyList(),
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val attachmentReferences: List<MailAttachmentReference> = emptyList(),
    val attachments: Map<String, ByteArrayDataSource> = emptyMap(),
    var reservation: MailReservation? = null,
) {
    fun schedule(
        groupId: Long,
        reservationTime: Instant,
    ) {
        reservation =
            MailReservation(
                groupId = groupId,
                reservationTime = reservationTime,
                mailId = this.id ?: 0L,
            )
    }
}

fun List<Mail>.schedule(
    groupId: Long,
    reservationTime: Instant,
) {
    forEach {
        it.schedule(groupId, reservationTime)
    }
}

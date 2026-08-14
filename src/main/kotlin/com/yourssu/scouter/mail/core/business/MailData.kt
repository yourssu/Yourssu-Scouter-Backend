package com.yourssu.scouter.mail.core.business

import com.yourssu.scouter.mail.core.implement.MailReservation
import jakarta.mail.util.ByteArrayDataSource

data class MailData(
    val senderEmailAddress: String,
    val receiverEmailAddresses: List<String> = emptyList(),
    val ccEmailAddresses: List<String> = emptyList(),
    val bccEmailAddresses: List<String> = emptyList(),
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val attachments: Map<String, ByteArrayDataSource> = emptyMap(),
) {
    companion object {
        fun from(reservation: MailReservation): MailData {
            return MailData(
                // 실제 From은 GoogleMailSender가 공용 발송 계정으로 덮어쓴다
                senderEmailAddress = "",
                receiverEmailAddresses = listOf(reservation.receiverEmailAddress),
                ccEmailAddresses = reservation.ccEmailAddresses,
                bccEmailAddresses = reservation.bccEmailAddresses,
                mailSubject = reservation.mailSubject,
                mailBody = reservation.mailBody,
                bodyFormat = reservation.bodyFormat,
            )
        }
    }
}

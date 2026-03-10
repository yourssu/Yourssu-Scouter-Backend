package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailReservationDetail
import com.yourssu.scouter.common.implement.domain.mail.MailReservationStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class MailReservationDetailResponse(
    val reservationId: Long,
    val mailId: Long,
    val reservationTime: Instant,
    @Schema(description = "예약 상태", example = "SCHEDULED", allowableValues = ["SCHEDULED", "PENDING_SEND", "SENT"])
    val status: MailReservationStatus,
    @Schema(description = "발신자 이메일", example = "sender@example.com")
    val senderEmailAddress: String,
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: String,
    val receiverEmailAddresses: List<String>,
    val ccEmailAddresses: List<String>,
    val bccEmailAddresses: List<String>,
    val hasAttachments: Boolean,
) {
    companion object {
        fun from(detail: MailReservationDetail): MailReservationDetailResponse {
            return MailReservationDetailResponse(
                reservationId = detail.reservationId,
                mailId = detail.mailId,
                reservationTime = detail.reservationTime,
                status = detail.status,
                senderEmailAddress = detail.senderEmailAddress,
                mailSubject = detail.mailSubject,
                mailBody = detail.mailBody,
                bodyFormat = detail.bodyFormat.name,
                receiverEmailAddresses = detail.receiverEmailAddresses,
                ccEmailAddresses = detail.ccEmailAddresses,
                bccEmailAddresses = detail.bccEmailAddresses,
                hasAttachments = detail.hasAttachments,
            )
        }
    }
}


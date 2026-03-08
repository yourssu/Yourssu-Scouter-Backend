package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailReservationDetail
import com.yourssu.scouter.common.implement.domain.mail.MailReservationStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class MailReservationListItem(
    val reservationId: Long,
    val mailId: Long,
    val reservationTime: Instant,
    @Schema(description = "예약 상태", example = "SCHEDULED", allowableValues = ["SCHEDULED", "PENDING_SEND", "SENT"])
    val status: MailReservationStatus,
    val mailSubject: String,
    @Schema(description = "대표 수신자 이메일 (수신자가 없으면 null)", example = "receiver@example.com", nullable = true)
    val primaryReceiverEmailAddress: String?,
    val hasAttachments: Boolean,
)

data class MailReservationListResponse(
    val items: List<MailReservationListItem>,
) {
    companion object {
        fun from(details: List<MailReservationDetail>): MailReservationListResponse {
            return MailReservationListResponse(
                items =
                    details.map { detail ->
                        MailReservationListItem(
                            reservationId = detail.reservationId,
                            mailId = detail.mailId,
                            reservationTime = detail.reservationTime,
                            status = detail.status,
                            mailSubject = detail.mailSubject,
                            primaryReceiverEmailAddress = detail.receiverEmailAddresses.firstOrNull(),
                            hasAttachments = detail.hasAttachments,
                        )
                    },
            )
        }
    }
}


package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailReservationDetail
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class MailReservationListItem(
    val reservationId: Long,
    val mailId: Long,
    val reservationTime: Instant,
    @Schema(description = "예약 상태", example = "SCHEDULED", allowableValues = ["SCHEDULED", "PENDING_SEND", "SENT"])
    val status: String,
    val mailSubject: String,
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
                            status = detail.status.name,
                            mailSubject = detail.mailSubject,
                            primaryReceiverEmailAddress = detail.receiverEmailAddresses.firstOrNull(),
                            hasAttachments = detail.hasAttachments,
                        )
                    },
            )
        }
    }
}


package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailReservationDetail
import java.time.Instant

data class MailReservationListItem(
    val reservationId: Long,
    val mailId: Long,
    val reservationTime: Instant,
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
                            mailSubject = detail.mailSubject,
                            primaryReceiverEmailAddress = detail.receiverEmailAddresses.firstOrNull(),
                            hasAttachments = detail.hasAttachments,
                        )
                    },
            )
        }
    }
}


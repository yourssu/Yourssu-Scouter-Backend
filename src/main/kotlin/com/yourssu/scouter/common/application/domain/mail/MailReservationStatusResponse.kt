package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.PendingMailReservationStatus
import java.time.Instant

data class MailReservationStatusItem(
    val reservationId: Long,
    val reservationTime: Instant,
    val failureErrorCode: String?,
    val failedAt: Instant?,
)

data class MailReservationStatusResponse(
    val items: List<MailReservationStatusItem>,
) {
    companion object {
        fun from(statuses: List<PendingMailReservationStatus>): MailReservationStatusResponse {
            return MailReservationStatusResponse(
                items = statuses.map { s ->
                    MailReservationStatusItem(
                        reservationId = s.reservationId,
                        reservationTime = s.reservationTime,
                        failureErrorCode = s.failureErrorCode,
                        failedAt = s.failedAt,
                    )
                },
            )
        }
    }
}

package com.yourssu.scouter.mail.application

import com.yourssu.scouter.mail.business.PendingMailReservationStatus
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

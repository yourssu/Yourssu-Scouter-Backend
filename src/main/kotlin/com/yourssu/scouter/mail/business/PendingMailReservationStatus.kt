package com.yourssu.scouter.mail.business

import java.time.Instant

data class PendingMailReservationStatus(
    val reservationId: Long,
    val reservationTime: Instant,
    val failureErrorCode: String?,
    val failedAt: Instant?,
)

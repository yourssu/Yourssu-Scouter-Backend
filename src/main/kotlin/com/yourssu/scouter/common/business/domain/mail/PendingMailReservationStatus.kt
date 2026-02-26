package com.yourssu.scouter.common.business.domain.mail

import java.time.Instant

data class PendingMailReservationStatus(
    val reservationId: Long,
    val mailId: Long,
    val reservationTime: Instant,
    val failureErrorCode: String?,
    val failedAt: Instant?,
)

package com.yourssu.scouter.common.implement.domain.mail

import java.time.Instant

data class MailReservation(
    val id: Long? = null,
    val mailId: Long,
    val reservationTime: Instant,
    val status: MailReservationStatus = MailReservationStatus.SCHEDULED,
    val claimedAt: Instant? = null,
)

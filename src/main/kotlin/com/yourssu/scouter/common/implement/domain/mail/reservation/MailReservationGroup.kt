package com.yourssu.scouter.common.implement.domain.mail.reservation

import java.time.Instant

data class MailReservationGroup(
    val id: Long? = null,
    val senderEmail: String,
    val templateId: Long?,
    val reservationTime: Instant,
    val status: MailReservationStatus = MailReservationStatus.SCHEDULED,
    val createdAt: Instant = Instant.now(),
)

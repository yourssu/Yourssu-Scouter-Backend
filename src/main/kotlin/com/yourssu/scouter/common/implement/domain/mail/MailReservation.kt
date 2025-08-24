package com.yourssu.scouter.common.implement.domain.mail

import java.time.LocalDateTime

class MailReservation(
    val id: Long? = null,
    val mailId: Long,
    val reservationTime: LocalDateTime,
)

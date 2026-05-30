package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationStatus
import java.time.Instant

data class MailGroupDetail(
    val groupId: Long,
    val senderEmail: String,
    val templateId: Long?,
    val reservationTime: Instant,
    val status: MailReservationStatus,
    val createdAt: Instant,
    val mailIds: List<Long>,
)

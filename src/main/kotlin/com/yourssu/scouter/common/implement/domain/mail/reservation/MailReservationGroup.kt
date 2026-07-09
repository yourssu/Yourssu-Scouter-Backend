package com.yourssu.scouter.common.implement.domain.mail.reservation

import java.time.Instant

data class MailReservationGroup(
    val id: Long? = null,
    // 예약자 users.id (null: 레거시 backfill 실패 데이터)
    val reservedByUserId: Long?,
    val templateId: Long?,
    val reservationTime: Instant,
    val status: MailReservationStatus = MailReservationStatus.SCHEDULED,
    val createdAt: Instant = Instant.now(),
)

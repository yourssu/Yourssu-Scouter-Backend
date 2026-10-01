package com.yourssu.scouter.mail.core.implement

import java.time.Instant

data class MailReservationGroup(
    val id: Long? = null,
    // 예약자 users.id (null: 레거시 backfill 실패 데이터)
    val reservedByUserId: Long?,
    val templateId: Long?,
    val reservationTime: Instant,
    val status: MailReservationStatus = MailReservationStatus.SCHEDULED,
    val createdAt: Instant = Instant.now(),
) {
    companion object {
        /**
         * 소속 메일 상태들로부터 그룹 상태를 계산한다. 소속 메일이 없으면 null.
         *
         * SENDING 우선 → 전부 SENT → PENDING_SEND 존재 → 그 외 SCHEDULED
         */
        fun resolveStatus(reservationStatuses: Collection<MailReservationStatus>): MailReservationStatus? {
            if (reservationStatuses.isEmpty()) {
                return null
            }
            return when {
                MailReservationStatus.SENDING in reservationStatuses -> MailReservationStatus.SENDING
                reservationStatuses.all { it == MailReservationStatus.SENT } -> MailReservationStatus.SENT
                MailReservationStatus.PENDING_SEND in reservationStatuses -> MailReservationStatus.PENDING_SEND
                else -> MailReservationStatus.SCHEDULED
            }
        }
    }
}
